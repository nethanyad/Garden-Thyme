package com.gardenthyme.dao;

import com.gardenthyme.models.User;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import com.gardenthyme.dao.UserDAO;
import com.gardenthyme.models.User;

/**
 * UserDAO handles all database operations for the USERS table.
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class UserDAO {
    
    private Connection connection;
    private UserDAO userDAO;
    
    public UserDAO(Connection connection) {
        this.connection = connection;
    }
    
    public void setConnection(Connection conn) {
    this.userDAO = new UserDAO(conn);
}
    
    /**
     * Register a new user in the system.
     * 
     * @param user The user object containing registration info
     * @return True if registration successful, false otherwise
     */
    public boolean register(User user) {
        String sql = "INSERT INTO USERS (User_ID, Username, Email, Password) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user.getUserID());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Authenticate a user login.
     * 
     * @param username The username
     * @param password The password
     * @return User object if login successful, null otherwise
     */
    public User login(String email, String password) {
    String sql = "SELECT * FROM USERS WHERE Email = ? AND Password = ?";

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setString(1, email);
        pstmt.setString(2, password);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            User user = new User();
            user.setUserID(rs.getInt("User_ID"));
            user.setUsername(rs.getString("Username"));
            user.setEmail(rs.getString("Email"));
            user.setPassword(rs.getString("Password"));
            return user;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}
    
    /**
     * Log out a user (session management placeholder).
     * 
     * @param userID The ID of the user logging out
     * @return True if logout successful
     */
    public boolean logout(int userID) {
        System.out.println("User " + userID + " logged out successfully.");
        return true;
    }
    
    /**
     * Update a user's username.
     * 
     * @param userID The ID of the user
     * @param newUsername The new username
     * @return True if update successful, false otherwise
     */
    public boolean updateUsername(int userID, String newUsername) {
        String sql = "UPDATE USERS SET Username = ? WHERE User_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newUsername);
            pstmt.setInt(2, userID);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update a user's password.
     * 
     * @param userID The ID of the user
     * @param newPassword The new password
     * @return True if update successful, false otherwise
     */
    public boolean updatePassword(int userID, String newPassword) {
        String sql = "UPDATE USERS SET Password = ? WHERE User_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userID);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Find a user by ID.
     * 
     * @param userID The user ID to search for
     * @return User object if found, null otherwise
     */
    public User findByID(int userID) {
        String sql = "SELECT * FROM USERS WHERE User_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("User_ID"));
                user.setUsername(rs.getString("Username"));
                user.setEmail(rs.getString("Email"));
                user.setPassword(rs.getString("Password"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find a user by Username.
     *
     * @param username The username to search for
     * @return User object if found, null otherwise
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM USERS WHERE Username = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("User_ID"));
                user.setUsername(rs.getString("Username"));
                user.setEmail(rs.getString("Email"));
                user.setPassword(rs.getString("Password"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all users.
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM USERS";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("User_ID"));
                user.setUsername(rs.getString("Username"));
                user.setEmail(rs.getString("Email"));
                user.setPassword(rs.getString("Password"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean createUser(User user, String role) {
    String sql = "INSERT INTO USERS (User_ID, Username, Email, Password) VALUES (?, ?, ?, ?)";

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

        int newId = (int) (Math.random() * 100000); // simple ID generator (ok for project)

        pstmt.setInt(1, newId);
        pstmt.setString(2, user.getUsername());
        pstmt.setString(3, user.getEmail());
        pstmt.setString(4, user.getPassword());

        int rows = pstmt.executeUpdate();

        if (rows > 0) {
            if (role.equals("ADMIN")) {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO ADMIN (User_ID) VALUES (?)"
                );
                ps.setInt(1, newId);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO GARDENER (User_ID) VALUES (?)"
                );
                ps.setInt(1, newId);
                ps.executeUpdate();
            }
        }

        return rows > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
}
