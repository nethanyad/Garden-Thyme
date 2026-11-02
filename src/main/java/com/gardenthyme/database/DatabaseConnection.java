package com.gardenthyme.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection manager.
 * Each team member should edit this file to include their personal password and keep it local.
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/GardenDB";
    private static final String USER = "root";
    private static final String PASSWORD = "mypassword123"; // Update with your password
    
    private static Connection connection = null;
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database!");
            e.printStackTrace();
        }
        return connection;
    }
    
    public static void closeConnection() {
        // Don't auto-close - let the application manage this
        // This prevents the connection from being closed between screens
    }
    
    public static void shutdown() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
