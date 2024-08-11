package projects.dao;

import projects.exception.DbException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private static String HOST = "localhost";
    private static String PASSWORD = "projects";
    private static int PORT = 3306;
    private static String SCHEMA = "projects";
    private static String USER = "projects";

    public static Connection getConnection() {
        String uri = "jdbc:mysql://" + HOST + ":" + PORT + "/" + SCHEMA;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(uri, USER, PASSWORD);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("Manual Message: Error Connecting to the Database");
            throw new DbException("Unable to Connect to the Database", e);
        }
        return connection;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new DbException("Error closing connection", e);
            }
        }
    }
}