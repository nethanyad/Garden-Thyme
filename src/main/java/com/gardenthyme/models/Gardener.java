package com.gardenthyme.models;

/**
 * Gardener class representing regular users who maintain gardens.
 * Extends the base User class.
 * 
 * Note: In the EERD, Admin and Gardener are overlapping and total with User.
 * A User must be either a Gardener, Admin, or Both.
 * 
 * Gardener-specific operations (viewing plants, managing tasks, etc.)
 * are handled by the GardenDAO and TaskDAO classes, not as methods on this model.
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class Gardener extends User {
    
    /**
     * Default constructor
     */
    public Gardener() {
        super();
    }
    
    /**
     * Constructor with basic gardener information
     * @param userID Unique identifier for the gardener
     * @param username Gardener's username
     * @param email Gardener's email address
     * @param password Gardener's password
     */
    public Gardener(int userID, String username, String email, String password) {
        super(userID, username, email, password);
    }
    
    @Override
    public String toString() {
        return "Gardener{userID=" + getUserID() + ", username='" + getUsername() + "'}";
    }
}
