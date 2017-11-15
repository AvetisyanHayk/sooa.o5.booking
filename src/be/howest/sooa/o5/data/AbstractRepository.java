package be.howest.sooa.o5.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Hayk
 */
public abstract class AbstractRepository {

    private static final String URL = "jdbc:%s://%s:%s/%s?useSSL=%s";
    private static String _url;
    private static String _user;
    private static String _password;

    protected static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(_url, _user, _password);
    }

    public static void connect(String driver, String hostname, String port,
            String database, String user, String password, String useSSL)
            throws SQLException {
        String url = String.format(URL, driver, hostname, port, database, useSSL);
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            setConnectionDetails(driver, hostname, port, database, user, password, useSSL);
        }
    }

    private static void setConnectionDetails(String driver, String hostname,
            String port, String database, String user, String password, String useSSL) {
        _url = String.format(URL, driver, hostname, port, database, useSSL);
        _user = user;
        _password = password;
    }
}
