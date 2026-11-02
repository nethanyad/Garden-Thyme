package com.gardenthyme.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gardenthyme.models.Pest;
import com.gardenthyme.models.Plant;

/**
 * AdminDAO handles all database operations for admin users.
 * - addPlant()
 * - modifyPlant()
 * - deletePlant()
 * - addPest()
 * - modifyPest()
 * - deletePest()
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class AdminDAO {
    
    private Connection connection;
    private PlantDAO plantDAO;
    private PestDAO pestDAO;
    
    public AdminDAO(Connection connection) {
        this.connection = connection;
        this.plantDAO = new PlantDAO();
        this.pestDAO = new PestDAO();
    }
    
    /**
     * Check if a user is an admin (exists in ADMIN table).
     * 
     * @param userID The user ID to check
     * @return True if user is an admin, false otherwise
     */
    public boolean isAdmin(int userID) {
        String sql = "SELECT * FROM ADMIN WHERE User_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add a user as an admin (insert into ADMIN table).
     * 
     * @param userID The user ID to make an admin
     * @return True if successful, false otherwise
     */
    public boolean addAdmin(int userID) {
        String checkUserSql = "SELECT * FROM USERS WHERE User_ID = ?";
        String insertAdminSql = "INSERT INTO ADMIN (User_ID) VALUES (?)";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkUserSql)) {
            checkStmt.setInt(1, userID);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("User ID " + userID + " does not exist.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertAdminSql)) {
            pstmt.setInt(1, userID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== Plant Management Methods =====
    
    /**
     * Add a new plant to the system.
     * 
     * @param adminID The ID of the admin performing this action
     * @param plant The plant object to add
     * @return True if addition successful, false otherwise
     */
    public boolean addPlant(int adminID, Plant plant) {
        if (!isAdmin(adminID)) {
            System.out.println("User " + adminID + " is not an admin.");
            return false;
        }
        
        try {
            plantDAO.addPlant(plant, connection);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Modify an existing plant.
     * 
     * @param adminID The ID of the admin performing this action
     * @param plant The plant object with updated information
     * @return True if modification successful, false otherwise
     */
    public boolean modifyPlant(int adminID, Plant plant) {
        if (!isAdmin(adminID)) {
            System.out.println("User " + adminID + " is not an admin.");
            return false;
        }
        
        try {
            plantDAO.updatePlant(plant, connection);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a plant from the system.
     * 
     * @param adminID The ID of the admin performing this action
     * @param plantID The ID of the plant to delete
     * @return True if deletion successful, false otherwise
     */
    public boolean deletePlant(int adminID, int plantID) {
        if (!isAdmin(adminID)) {
            System.out.println("User " + adminID + " is not an admin.");
            return false;
        }
        
        try {
            plantDAO.deletePlant(plantID, connection);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== Pest Management Methods =====
    
    /**
     * Add a new pest to the system.
     * 
     * @param adminID The ID of the admin performing this action
     * @param pest The pest object to add
     * @return True if addition successful, false otherwise
     */
    public boolean addPest(int adminID, Pest pest) {
        if (!isAdmin(adminID)) {
            System.out.println("User " + adminID + " is not an admin.");
            return false;
        }
        
        try {
            pestDAO.addPest(pest, connection);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Modify an existing pest.
     * 
     * @param adminID The ID of the admin performing this action
     * @param pest The pest object with updated information
     * @return True if modification successful, false otherwise
     */
    public boolean modifyPest(int adminID, Pest pest) {
        if (!isAdmin(adminID)) {
            System.out.println("User " + adminID + " is not an admin.");
            return false;
        }
        
        try {
            pestDAO.updatePest(pest, connection);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a pest from the system.
     * 
     * @param adminID The ID of the admin performing this action
     * @param pestName The name of the pest to delete
     * @return True if deletion successful, false otherwise
     */
    public boolean deletePest(int adminID, String pestName) {
        if (!isAdmin(adminID)) {
            System.out.println("User " + adminID + " is not an admin.");
            return false;
        }
        
        try {
            pestDAO.deletePest(pestName, connection);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
