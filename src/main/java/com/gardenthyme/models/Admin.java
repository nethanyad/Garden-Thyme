package com.gardenthyme.models;

/**
 * Admin class representing users with administrative privileges.
 * Extends the base User class.
 * 
 * Note: In the EERD, Admin and Gardener are overlapping and total with User.
 * A User must be either a Gardener, Admin, or Both.
 * 
 * The actual admin operations (addPlant, modifyPlant, etc.) are implemented
 * in the DAO layer, not as methods on the model object.
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class Admin extends User {
    
    /**
     * Default constructor
     */
    public Admin() {
        super();
    }
    
    /**
     * Constructor with basic admin information
     * @param userID Unique identifier for the admin
     * @param username Admin's username
     * @param email Admin's email address
     * @param password Admin's password
     */
    public Admin(int userID, String username, String email, String password) {
        super(userID, username, email, password);
    }
    
    @Override
    public String toString() {
        return "Admin{userID=" + getUserID() + ", username='" + getUsername() + "'}";
    }
}
