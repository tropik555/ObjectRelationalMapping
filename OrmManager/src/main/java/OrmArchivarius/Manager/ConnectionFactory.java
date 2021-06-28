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
/*
        Connection connection=null;
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "database.properties";

        try {
            Properties properties = new Properties();
            try (InputStream inputStream = Files.newInputStream(Paths.get("src/main/resources/database.properties"))) {
                properties.load(inputStream);

            }
            String databaseName = properties.getProperty("DBurl");
            String userName = properties.getProperty("DBusername");
            String userPassword = properties.getProperty("DBpassword");
            connection = DriverManager.getConnection(databaseName, userName, userPassword);

        } catch (IOException e) {
            e.printStackTrace();
        }
*/

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
