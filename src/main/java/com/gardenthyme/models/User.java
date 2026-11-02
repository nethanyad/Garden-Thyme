package com.gardenthyme.models;

/**
 * Base User class representing the USERS table in the database.
 * This is the parent class for Admin and Gardener.
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class User {
    // Primary key from USERS table
    protected int userID;

    // User's unique username
    protected String username;

    // User's email address (unique in database)
    protected String email;

    // User's password
    protected String password;
    
    // Default constructor
    public User() {}
    
    // Parameterized constructor
    public User(int userID, String username, String email, String password) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // ===== Getters and Setters =====
    
    public int getUserID() {
        return userID;
    }
    
    public void setUserID(int userID) {
        this.userID = userID;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "User{userID=" + userID + ", username='" + username + "'}";
    }
}
