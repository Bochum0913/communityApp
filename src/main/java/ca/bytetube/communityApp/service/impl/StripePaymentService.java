package ca.bytetube.communityApp.service.impl;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/** Stripe Checkout integration used only with Stripe test credentials. */
@Service
public class StripePaymentService {
    private final JdbcTemplate db;
    private final CommerceService orders;

    @Autowired
    public StripePaymentService(DataSource dataSource, CommerceService orders) {
        this.db = new JdbcTemplate(dataSource);
        this.orders = orders;
    }

    private String requiredEnv(String name, String prefix) {
        String value = System.getenv(name);
        if (value == null || !value.startsWith(prefix)) {
            throw new IllegalStateException(name + " is not configured for Stripe Test Mode.");
        }
        return value;
    }

    private StripeClient testClient() {
        return new StripeClient(requiredEnv("STRIPE_SECRET_KEY", "sk_test_"));
    }

    /** Create a priced order using DB product data, then redirect to hosted Checkout. */
    public CheckoutResult checkout(long customerId, long productId, int quantity) throws StripeException {
        // Verify configuration before creating an unpaid order.
        StripeClient client = testClient();
        String base = requiredBaseUrl();
        long orderId = orders.placeOrder(customerId, productId, quantity);
        try {
            List<Map<String,Object>> rows = db.queryForList(
                "SELECT o.customer_id,o.status,o.total_amount,i.quantity,i.unit_price,i.product_name " +
                "FROM ca_order o JOIN ca_order_item i ON i.order_id=o.order_id WHERE o.order_id=?", orderId);
            if (rows.size() != 1 || ((Number)rows.get(0).get("customer_id")).longValue() != customerId) {
                throw new IllegalStateException("Order was not created correctly.");
            }
            Map<String,Object> row = rows.get(0);
            BigDecimal price = ((BigDecimal) row.get("unit_price")).setScale(2, RoundingMode.UNNECESSARY);
            long cents = price.movePointRight(2).longValueExact();
            if (cents <= 0) throw new IllegalStateException("Checkout requires a positive product price.");
            String itemName = (String) row.get("product_name");
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(base + "/frontend/paymentresult?orderId=" + orderId + "&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(base + "/frontend/paymentresult?orderId=" + orderId + "&cancelled=1")
                .setClientReferenceId(String.valueOf(orderId))
                .putMetadata("community_order_id", String.valueOf(orderId))
                .addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(((Number)row.get("quantity")).longValue())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("cad")
                        .setUnitAmount(cents)
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(itemName).build())
                        .build())
                    .build())
                .build();
            RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey("community-checkout-order-" + orderId).build();
            Session session = client.v1().checkout().sessions().create(params, options);
            if (session.getId() == null || session.getUrl() == null || !session.getUrl().startsWith("https://checkout.stripe.com/")) {
                throw new IllegalStateException("Stripe did not return a secure Checkout URL.");
            }
            int saved = db.update("UPDATE ca_order SET stripe_checkout_session_id=?, payment_status='PENDING' " +
                "WHERE order_id=? AND customer_id=? AND status='PENDING_PAYMENT' AND stripe_checkout_session_id IS NULL",
                session.getId(), orderId, customerId);
            if (saved != 1) throw new IllegalStateException("Cannot attach the Stripe Checkout Session to the order.");
            return new CheckoutResult(orderId, session.getUrl());
        } catch (Exception ex) {
            db.update("UPDATE ca_order SET status='PAYMENT_FAILED',payment_status='FAILED',updated_at=NOW() " +
                "WHERE order_id=? AND status='PENDING_PAYMENT' AND stripe_checkout_session_id IS NULL",orderId);
            if (ex instanceof StripeException) throw (StripeException) ex;
            throw new IllegalStateException("Unable to create Checkout Session.", ex);
        }
    }

    private String requiredBaseUrl() {
        String base = System.getenv("APP_PUBLIC_BASE_URL");
        if (base == null || !(base.startsWith("http://localhost:8080/communityApp") ||
                base.startsWith("http://127.0.0.1:8080/communityApp"))) {
            throw new IllegalStateException("APP_PUBLIC_BASE_URL must be the local communityApp URL for this test-only patch.");
        }
        return base.replaceAll("/+$", "");
    }

    @Transactional
    public void markPaid(String eventId, long orderId, String sessionId, String paymentIntentId,
                         String currency, long amountTotal, boolean liveMode) {
        if (liveMode || !"cad".equalsIgnoreCase(currency) || amountTotal <= 0) {
            throw new IllegalArgumentException("Invalid Stripe test payment.");
        }
        // Unique Stripe event IDs and conditional status transition protect against duplicate webhook delivery.
        int inserted = db.update("INSERT IGNORE INTO ca_stripe_event(event_id,order_id,created_at) VALUES(?,?,NOW())",eventId,orderId);
        if (inserted == 0) return;
        List<Map<String,Object>> rows = db.queryForList(
            "SELECT status,total_amount,stripe_checkout_session_id FROM ca_order WHERE order_id=? FOR UPDATE",orderId);
        if (rows.size() != 1) throw new IllegalArgumentException("Unknown order.");
        Map<String,Object> row = rows.get(0);
        if (!sessionId.equals(row.get("stripe_checkout_session_id"))) throw new IllegalArgumentException("Session mismatch.");
        long expected = ((BigDecimal)row.get("total_amount")).movePointRight(2).longValueExact();
        if (expected != amountTotal) throw new IllegalArgumentException("Stripe amount mismatch.");
        if ("PLACED".equals(row.get("status")) || "COMPLETED".equals(row.get("status"))) return;
        int updated = db.update("UPDATE ca_order SET status='PLACED',payment_status='PAID',stripe_payment_intent_id=?,updated_at=NOW() " +
            "WHERE order_id=? AND status='PENDING_PAYMENT' AND stripe_checkout_session_id=?",paymentIntentId,orderId,sessionId);
        if (updated != 1) throw new IllegalArgumentException("Order is not awaiting payment.");
    }

    @Transactional
    public void markFailed(String eventId, long orderId, String sessionId) {
        int inserted=db.update("INSERT IGNORE INTO ca_stripe_event(event_id,order_id,created_at) VALUES(?,?,NOW())",eventId,orderId);
        if (inserted==0)return;
        db.update("UPDATE ca_order SET status='PAYMENT_FAILED',payment_status='FAILED',updated_at=NOW() " +
            "WHERE order_id=? AND stripe_checkout_session_id=? AND status='PENDING_PAYMENT'",orderId,sessionId);
    }

    public static final class CheckoutResult {
        private final long orderId;
        private final String checkoutUrl;
        public CheckoutResult(long orderId,String checkoutUrl) {this.orderId=orderId;this.checkoutUrl=checkoutUrl;}
        public long getOrderId(){return orderId;}
        public String getCheckoutUrl(){return checkoutUrl;}
    }
}
