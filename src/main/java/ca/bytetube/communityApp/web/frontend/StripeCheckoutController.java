package ca.bytetube.communityApp.web.frontend;

import ca.bytetube.communityApp.entity.PersonInfo;
import ca.bytetube.communityApp.service.impl.StripePaymentService;
import ca.bytetube.communityApp.web.local.AccountController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/frontend/api")
public class StripeCheckoutController {
    @Autowired private StripePaymentService payments;
    private final ObjectMapper json = new ObjectMapper();

    @RequestMapping(value="/stripe-checkout",method=RequestMethod.POST)
    @ResponseBody public Map<String,Object> checkout(HttpServletRequest request,
                @RequestParam long productId,@RequestParam int quantity) {
        PersonInfo customer=(PersonInfo)request.getSession().getAttribute("user");
        if (customer==null || customer.getUserType()==null || customer.getUserType()!=1)
            return AccountController.result(false,"Customer sign-in required.");
        if (!AccountController.validCsrf(request)) return AccountController.result(false,"Invalid request token.");
        try {
            StripePaymentService.CheckoutResult session=payments.checkout(customer.getUserId(),productId,quantity);
            Map<String,Object> result=AccountController.result(true,"Continue to secure Stripe Checkout.");
            Map<String,Object> data=new HashMap<>();
            data.put("orderId",session.getOrderId());data.put("checkoutUrl",session.getCheckoutUrl());
            result.put("data",data);return result;
        } catch (StripeException ex) {
            return AccountController.result(false,"Stripe Checkout could not be created. Please try again.");
        } catch (Exception ex) {
            return AccountController.result(false,"Checkout is unavailable. Check server configuration and logs.");
        }
    }

    /** Stripe calls this endpoint directly; session cookies and CSRF are deliberately not required. */
    @RequestMapping(value="/stripe-webhook",method=RequestMethod.POST)
    @ResponseBody public ResponseEntity<String> webhook(@RequestBody String payload,
                         @RequestHeader(value="Stripe-Signature",required=false) String signature) {
        String secret=System.getenv("STRIPE_WEBHOOK_SECRET");
        if (secret==null || !secret.startsWith("whsec_") || signature==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook not configured.");
        try {
            Event event=Webhook.constructEvent(payload,signature,secret);
            String type=event.getType();
            if (!("checkout.session.completed".equals(type) ||
                  "checkout.session.async_payment_succeeded".equals(type) ||
                  "checkout.session.async_payment_failed".equals(type))) return ResponseEntity.ok("Ignored");
            JsonNode root=json.readTree(payload);
            JsonNode obj=root.path("data").path("object");
            if (!"checkout.session".equals(obj.path("object").asText()))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid object.");
            String sessionId=obj.path("id").asText();
            String orderText=obj.path("metadata").path("community_order_id").asText();
            long orderId=Long.parseLong(orderText);
            if (orderId<=0 || sessionId.isEmpty()) throw new IllegalArgumentException("Invalid IDs");
            boolean liveMode=root.path("livemode").asBoolean(true);
            if (liveMode) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Live mode is disabled.");
            if ("checkout.session.async_payment_failed".equals(type)) {
                payments.markFailed(event.getId(),orderId,sessionId);
            } else if ("paid".equals(obj.path("payment_status").asText())) {
                payments.markPaid(event.getId(),orderId,sessionId,
                    obj.path("payment_intent").asText(null),obj.path("currency").asText(),
                    obj.path("amount_total").asLong(-1),liveMode);
            }
            return ResponseEntity.ok("ok");
        } catch (SignatureVerificationException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid webhook.");
        } catch (Exception ex) {
            // A 5xx response instructs Stripe to retry transient failures.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed.");
        }
    }
}
