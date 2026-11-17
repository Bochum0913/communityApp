
package ca.bytetube.community;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDatabaseConnection extends BaseTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void verifyTestDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT DATABASE()")) {

            Assert.assertTrue(result.next());
            Assert.assertEquals("community_app_test", result.getString(1));

            System.out.println("Connected to: " + result.getString(1));
        }
    }
}
