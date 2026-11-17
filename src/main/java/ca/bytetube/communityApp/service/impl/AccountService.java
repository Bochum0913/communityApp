package ca.bytetube.communityApp.service.impl;

import ca.bytetube.communityApp.entity.PersonInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {
    private final JdbcTemplate db;
    private static final int ITERATIONS = 210000;
    private static final SecureRandom RNG = new SecureRandom();

    @Autowired
    public AccountService(DataSource source) { this.db = new JdbcTemplate(source); }

    private String hash(String password) {
        try {
            byte[] salt = new byte[16];
            RNG.nextBytes(salt);
            byte[] derived = derive(password, salt, ITERATIONS);
            return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                    + "$" + Base64.getEncoder().encodeToString(derived);
        } catch (Exception e) { throw new IllegalStateException("Password hashing unavailable", e); }
    }

    private byte[] derive(String password, byte[] salt, int iterations) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }

    private boolean matches(String password, String encoded) {
        try {
            String[] parts = encoded.split("\\$");
            if (parts.length != 4 || !"pbkdf2".equals(parts[0])) return false;
            int count = Integer.parseInt(parts[1]);
            if (count < 100000 || count > 2000000) return false;
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, derive(password, salt, count));
        } catch (Exception e) { return false; }
    }

    private void requirePassword(String password) {
        if (password == null || password.length() < 10 || password.length() > 128
                || !password.matches("(?s).*\\p{Upper}.*") || !password.matches("(?s).*\\d.*"))
            throw new IllegalArgumentException("Use 10-128 characters including an uppercase letter and a number.");
    }

    public PersonInfo currentUser(long id) {
        List<Map<String,Object>> records = db.queryForList(
                "SELECT user_id,name,email,user_type,enable_status FROM tb_person_info WHERE user_id=?", id);
        if (records.isEmpty() || ((Number)records.get(0).get("enable_status")).intValue()!=1) return null;
        Map<String,Object> row=records.get(0);
        PersonInfo user=new PersonInfo();
        user.setUserId(((Number)row.get("user_id")).longValue());
        user.setName((String)row.get("name"));
        user.setEmail((String)row.get("email"));
        user.setUserType(((Number)row.get("user_type")).intValue());
        user.setEnableStatus(1);
        return user;
    }

    @Transactional
    public PersonInfo register(String username, String password, String email, String role) {
        if (username==null || !username.matches("[A-Za-z0-9_]{3,30}"))
            throw new IllegalArgumentException("Username must contain 3-30 letters, digits or underscores.");
        requirePassword(password);
        if (email==null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || email.length()>150)
            throw new IllegalArgumentException("Enter a valid email address.");
        int type="merchant".equalsIgnoreCase(role)?2:1;
        try {
            KeyHolder key = new GeneratedKeyHolder();
            db.update(connection -> {
                PreparedStatement ps=connection.prepareStatement(
                    "INSERT INTO tb_person_info(name,email,enable_status,user_type,create_time,last_edit_time) " +
                    "VALUES(?,?,1,?,NOW(),NOW())", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1,username);ps.setString(2,email);ps.setInt(3,type);return ps;
            },key);
            long userId=key.getKey().longValue();
            db.update("INSERT INTO tb_local_auth(user_id,username,password,create_time,last_edit_time) " +
                    "VALUES(?,?,?,NOW(),NOW())", userId,username,hash(password));
            db.update("INSERT INTO ca_point_account(user_id,balance) VALUES(?,0)",userId);
            return currentUser(userId);
        } catch (DuplicateKeyException ex) { throw new IllegalArgumentException("Username or email already exists."); }
    }

    public PersonInfo login(String username,String password) {
        if(username==null || password==null) return null;
        List<Map<String,Object>> rows=db.queryForList(
          "SELECT a.user_id,a.password FROM tb_local_auth a JOIN tb_person_info p ON p.user_id=a.user_id " +
          "WHERE a.username=? AND p.enable_status=1",username);
        if(rows.isEmpty()) return null;
        Map<String,Object> row=rows.get(0);
        if(!matches(password,(String)row.get("password"))) return null;
        return currentUser(((Number)row.get("user_id")).longValue());
    }

    @Transactional
    public void changePassword(long userId,String oldPassword,String newPassword) {
        requirePassword(newPassword);
        List<Map<String,Object>> rows=db.queryForList("SELECT password FROM tb_local_auth WHERE user_id=?",userId);
        if(rows.isEmpty() || !matches(oldPassword,(String)rows.get(0).get("password")))
            throw new IllegalArgumentException("Current password is incorrect.");
        if(db.update("UPDATE tb_local_auth SET password=?,last_edit_time=NOW() WHERE user_id=?",
                hash(newPassword),userId)!=1) throw new IllegalStateException("Unable to update password.");
    }

    @Transactional
    public void bindEmail(long userId,String password,String email) {
        if(email==null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || email.length()>150)
            throw new IllegalArgumentException("Enter a valid email address.");
        List<Map<String,Object>> rows=db.queryForList("SELECT password FROM tb_local_auth WHERE user_id=?",userId);
        if(rows.isEmpty() || !matches(password,(String)rows.get(0).get("password")))
            throw new IllegalArgumentException("Password is incorrect.");
        try {
            db.update("UPDATE tb_person_info SET email=?,last_edit_time=NOW() WHERE user_id=?",email,userId);
        } catch(DuplicateKeyException ex){ throw new IllegalArgumentException("Email is already in use."); }
    }

    public boolean ownsProduct(long userId,long productId){
        Integer count=db.queryForObject(
          "SELECT COUNT(*) FROM tb_product p JOIN tb_shop s ON s.shop_id=p.shop_id " +
          "WHERE p.product_id=? AND s.owner_id=?",Integer.class,productId,userId);
        return count!=null && count==1;
    }

    public boolean ownsShop(long userId,long shopId){
        Integer count=db.queryForObject("SELECT COUNT(*) FROM tb_shop WHERE shop_id=? AND owner_id=?",
                Integer.class,shopId,userId);
        return count!=null && count==1;
    }
}
