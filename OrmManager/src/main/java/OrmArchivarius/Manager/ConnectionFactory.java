package OrmArchivarius.Manager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    public static Connection getConnection(String dbName) {


        Connection connection = null;
        try {
            Properties defaultProps = new Properties();
            InputStream rootPath = Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties");
            defaultProps.load(rootPath);
            String databaseHost = defaultProps.getProperty("DBhost");
            String databaseUrl =databaseHost+dbName;
            String userName = defaultProps.getProperty("DBusername");
            String userPassword = defaultProps.getProperty("DBpassword");
            connection = DriverManager.getConnection(databaseUrl, userName, userPassword);
            connection.setAutoCommit(false);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
