package ca.bytetube.communityApp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Service
public class CommerceService {
    private final JdbcTemplate db;
    @Autowired public CommerceService(DataSource dataSource){this.db=new JdbcTemplate(dataSource);}

    @Transactional
    public long placeOrder(long customerId,long productId,int quantity){
        if(quantity<1 || quantity>20)throw new IllegalArgumentException("Quantity must be between 1 and 20.");
        List<Map<String,Object>> items=db.queryForList(
          "SELECT p.product_id,p.shop_id,p.product_name,p.normal_price,p.promotion_price,p.point " +
          "FROM tb_product p JOIN tb_shop s ON s.shop_id=p.shop_id " +
          "WHERE p.product_id=? AND p.enable_status=1 AND s.enable_status=1",productId);
        if(items.isEmpty())throw new IllegalArgumentException("Product is not available.");
        Map<String,Object> product=items.get(0);
        String price=(String)(product.get("promotion_price")!=null?product.get("promotion_price"):product.get("normal_price"));
        BigDecimal unit;
        try{unit=new BigDecimal(price).setScale(2,java.math.RoundingMode.HALF_UP);}
        catch(Exception e){throw new IllegalStateException("Product price is invalid.");}
        if(unit.signum()<0 || unit.compareTo(new BigDecimal("1000000"))>0)
            throw new IllegalStateException("Product price is invalid.");
        BigDecimal total=unit.multiply(BigDecimal.valueOf(quantity));
        int points=product.get("point")==null?0:((Number)product.get("point")).intValue();
        if(points<0 || points>1000000)throw new IllegalStateException("Invalid reward points.");
        long shopId=((Number)product.get("shop_id")).longValue();
        KeyHolder key=new GeneratedKeyHolder();
        db.update(connection -> {
            PreparedStatement ps=connection.prepareStatement(
              "INSERT INTO ca_order(customer_id,shop_id,status,total_amount,points_awarded,created_at) " +
              "VALUES(?,?,'PENDING_PAYMENT',?,?,NOW())",Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1,customerId);ps.setLong(2,shopId);
            ps.setBigDecimal(3,total);ps.setInt(4,points*quantity);return ps;
        },key);
        long id=key.getKey().longValue();
        db.update("INSERT INTO ca_order_item(order_id,product_id,product_name,quantity,unit_price) VALUES(?,?,?,?,?)",
          id,productId,(String)product.get("product_name"),quantity,unit);
        return id;
    }

    public List<Map<String,Object>> customerOrders(long customerId){
        return db.queryForList("SELECT o.order_id,o.status,o.total_amount,o.points_awarded,o.payment_status,o.created_at," +
            "i.product_name,i.quantity,i.unit_price FROM ca_order o JOIN ca_order_item i ON o.order_id=i.order_id " +
            "WHERE o.customer_id=? ORDER BY o.order_id DESC LIMIT 100",customerId);
    }

    public List<Map<String,Object>> merchantOrders(long merchantId){
        return db.queryForList("SELECT o.order_id,o.status,o.total_amount,o.points_awarded,o.payment_status,o.created_at," +
            "i.product_name,i.quantity,i.unit_price FROM ca_order o JOIN ca_order_item i ON o.order_id=i.order_id " +
            "JOIN tb_shop s ON s.shop_id=o.shop_id WHERE s.owner_id=? ORDER BY o.order_id DESC LIMIT 100",merchantId);
    }

    public int balance(long customerId){
        List<Integer> balances=db.query("SELECT balance FROM ca_point_account WHERE user_id=?",
            (rs,n)->rs.getInt(1),customerId);
        return balances.isEmpty()?0:balances.get(0);
    }

    public List<Map<String,Object>> pointHistory(long customerId){
        return db.queryForList("SELECT entry_id,order_id,delta,description,created_at FROM ca_point_ledger " +
            "WHERE user_id=? ORDER BY entry_id DESC LIMIT 100",customerId);
    }

    @Transactional
    public void cancelOrder(long customerId,long orderId){
        int updated=db.update("UPDATE ca_order SET status='CANCELLED',updated_at=NOW() " +
            "WHERE order_id=? AND customer_id=? AND status='PLACED' AND (payment_status IS NULL OR payment_status='NOT_REQUIRED')",orderId,customerId);
        if(updated!=1)throw new IllegalArgumentException("Only your placed orders can be cancelled.");
    }

    @Transactional
    public void completeOrder(long merchantId,long orderId){
        List<Map<String,Object>> rows=db.queryForList(
            "SELECT o.customer_id,o.points_awarded FROM ca_order o JOIN tb_shop s ON s.shop_id=o.shop_id " +
            "WHERE o.order_id=? AND s.owner_id=? AND o.status='PLACED' AND o.payment_status='PAID' FOR UPDATE",orderId,merchantId);
        if(rows.isEmpty())throw new IllegalArgumentException("Order not found or cannot be completed.");
        int updated=db.update("UPDATE ca_order SET status='COMPLETED',updated_at=NOW() " +
            "WHERE order_id=? AND status='PLACED' AND payment_status='PAID'",orderId);
        if(updated!=1)throw new IllegalArgumentException("Order already processed.");
        long customerId=((Number)rows.get(0).get("customer_id")).longValue();
        int points=((Number)rows.get(0).get("points_awarded")).intValue();
        db.update("INSERT INTO ca_point_account(user_id,balance) VALUES(?,?) " +
            "ON DUPLICATE KEY UPDATE balance=balance+VALUES(balance)",customerId,points);
        db.update("INSERT INTO ca_point_ledger(user_id,order_id,delta,description,created_at) " +
            "VALUES(?,?,?,'Order completed',NOW())",customerId,orderId,points);
    }
}
