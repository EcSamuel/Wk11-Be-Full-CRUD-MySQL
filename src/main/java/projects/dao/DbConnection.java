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
        // a.     Create a String variable named uri that contains the MySQL connection URI.
        String uri = "jdbc:mysql://" + HOST + ":" + PORT + "/" + SCHEMA;
        Connection connection = null;

        try {
            // b.     Call DriverManager to obtain a connection. Pass the connection string (URI) to DriverManager.getConnection().
            connection = DriverManager.getConnection(uri, USER, PASSWORD);
            // d.     Print a message to the console (System.out.println) if the connection is successful.
            System.out.println("Connected to database");
            // c.      Surround the call to DriverManager.getConnection() with a try/catch block. The catch block should catch SQLException.
        } catch (SQLException e) {
            // I wasn't sure if the manual message would work so I printed it twice plus the throw
            System.err.println(e.getMessage());
            // e.     Print an error message to the console if the connection fails. Throw a DbException if the connection fails.
            System.err.println("Manual Message: Error Connecting to the Database");
            throw new DbException("Unable to Connect to the Database", e);
        }
        return connection;
    }
}