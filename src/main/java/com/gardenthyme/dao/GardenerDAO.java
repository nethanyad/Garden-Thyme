package com.gardenthyme.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gardenthyme.models.Pest;
import com.gardenthyme.models.Plant;
import com.gardenthyme.models.Task;

/**
 * GardenerDAO handles all database operations for gardener users.
 * 
 * Gardeners can:
 * - Add plants to gardens (addPlantToGarden)
 * - View plant information (viewPlantInfo)
 * - Manage tasks (manageTasks)
 * - View pest information (viewPestInfo)
 * - Leave a garden (leaveGarden)
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class GardenerDAO {
    
    private Connection connection;
    private PlantDAO plantDAO;
    private PestDAO pestDAO;
    private TaskDAO taskDAO;
    private GardenDAO gardenDAO;
    
    public GardenerDAO(Connection connection) {
        this.connection = connection;
        this.plantDAO = new PlantDAO();
        this.pestDAO = new PestDAO();
        this.taskDAO = new TaskDAO(connection);
        this.gardenDAO = new GardenDAO(connection);
    }
    
    /**
     * Check if a user is a gardener (exists in GARDENER table).
     * 
     * @param userID The user ID to check
     * @return True if user is a gardener, false otherwise
     */
    public boolean isGardener(int userID) {
        String sql = "SELECT * FROM GARDENER WHERE User_ID = ?";
        
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
     * Add a user as a gardener (insert into GARDENER table).
     * 
     * @param userID The user ID to make a gardener
     * @return True if successful, false otherwise
     */
    public boolean addGardener(int userID) {
        String checkUserSql = "SELECT * FROM USERS WHERE User_ID = ?";
        String insertGardenerSql = "INSERT INTO GARDENER (User_ID) VALUES (?)";
        
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
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertGardenerSql)) {
            pstmt.setInt(1, userID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== Gardener Methods =====
    
    /**
     * Leave a garden (remove yourself as a manager).
     * Owners cannot leave. They must transfer ownership or delete the garden.
     * 
     * @param userID The user leaving the garden
     * @param gardenID The garden to leave
     * @return True if successful, false otherwise
     */
    public boolean leaveGarden(int userID, int gardenID) {
        if (!isGardener(userID)) {
            System.out.println("User " + userID + " is not a gardener.");
            return false;
        }
        
        // Check if user is the owner (owners cannot leave)
        if (gardenDAO.isOwner(gardenID, userID)) {
            System.out.println("Owner cannot leave garden. Transfer ownership or delete the garden instead.");
            return false;
        }
        
        // Check if user has access to this garden
        if (!gardenDAO.canManageGarden(gardenID, userID)) {
            System.out.println("User " + userID + " does not have access to garden " + gardenID);
            return false;
        }
        
        // Remove the user from MANAGES
        return gardenDAO.removeFromGarden(gardenID, userID, userID);
    }
    
    /**
     * Add a plant to a specific garden box.
     * Both owners and managers can do this.
     * 
     * @param gardenerID The ID of the gardener performing this action
     * @param plantID The ID of the plant to add
     * @param gardenID The ID of the garden
     * @param boxID The ID of the box within the garden
     * @return True if successful, false otherwise
     */
    public boolean addPlantToGarden(int gardenerID, int plantID, int gardenID, int boxID) {
        if (!isGardener(gardenerID)) {
            System.out.println("User " + gardenerID + " is not a gardener.");
            return false;
        }
        
        // Check if the gardener has access to this garden (owner or manager)
        if (!gardenDAO.canManageGarden(gardenID, gardenerID)) {
            System.out.println("Gardener " + gardenerID + " does not have access to garden " + gardenID);
            return false;
        }
        
        // Check if the garden box exists
        String checkBoxSql = "SELECT * FROM GARDEN_BOX WHERE Garden_ID = ? AND Box_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkBoxSql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, boxID);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Garden box " + boxID + " does not exist in garden " + gardenID);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Add the plant to the garden box
        String sql = "INSERT INTO PLANTED_IN (Plant_ID, Garden_ID, Box_ID) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, plantID);
            pstmt.setInt(2, gardenID);
            pstmt.setInt(3, boxID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * View detailed information about a plant.
     * 
     * @param gardenerID The ID of the gardener (for permission check)
     * @param plantID The ID of the plant to view
     * @return Plant object with details, or null if not found or no permission
     */
    public Plant viewPlantInfo(int gardenerID, int plantID) {
        if (!isGardener(gardenerID)) {
            System.out.println("User " + gardenerID + " is not a gardener.");
            return null;
        }
        
        try {
            return plantDAO.getPlantByID(plantID, connection);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get all tasks assigned to a gardener.
     * 
     * @param gardenerID The ID of the gardener
     * @return List of tasks assigned to this gardener
     */
    public List<Task> manageTasks(int gardenerID) {
        if (!isGardener(gardenerID)) {
            System.out.println("User " + gardenerID + " is not a gardener.");
            return new ArrayList<>();
        }
        
        return taskDAO.getTasksByUser(gardenerID);
    }
    
    /**
     * Mark a specific task as complete.
     * 
     * @param gardenerID The ID of the gardener
     * @param taskID The ID of the task to complete
     * @param plantID The plant ID associated with the task
     * @return True if successful, false otherwise
     */
    public boolean completeTask(int gardenerID, int taskID, int plantID) {
        if (!isGardener(gardenerID)) {
            System.out.println("User " + gardenerID + " is not a gardener.");
            return false;
        }
        
        // Verify the task is assigned to this gardener
        List<Integer> assignedUsers = taskDAO.getAssignedUsers(taskID, plantID);
        if (!assignedUsers.contains(gardenerID)) {
            System.out.println("Task " + taskID + " is not assigned to gardener " + gardenerID);
            return false;
        }
        
        // Use TaskDAO to complete the task
        return taskDAO.completeTask(taskID, plantID, true);
    }
    
    /**
     * View information about a pest.
     * 
     * @param gardenerID The ID of the gardener
     * @param pestName The name of the pest to view
     * @return Pest object with details, or null if not found
     */
    public Pest viewPestInfo(int gardenerID, String pestName) {
        if (!isGardener(gardenerID)) {
            System.out.println("User " + gardenerID + " is not a gardener.");
            return null;
        }
        
        try {
            return pestDAO.getPestByID(pestName, connection);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // ===== Garden Access Helper Methods =====
    
    /**
     * Check if a gardener has access to a garden (owner or manager).
     * 
     * @param gardenerID The gardener ID
     * @param gardenID The garden ID
     * @return True if the gardener has access, false otherwise
     */
    public boolean hasGardenAccess(int gardenerID, int gardenID) {
        if (!isGardener(gardenerID)) {
            return false;
        }
        return gardenDAO.canManageGarden(gardenID, gardenerID);
    }
    
    /**
     * Get all garden IDs a gardener has access to (both owned and managed).
     * 
     * @param gardenerID The gardener ID
     * @return List of garden IDs
     */
    public List<Integer> getAccessibleGardens(int gardenerID) {
        if (!isGardener(gardenerID)) {
            return new ArrayList<>();
        }
        return gardenDAO.getGardenIDsByUser(gardenerID);
    }
    
    /**
     * Get all garden IDs a gardener owns.
     * 
     * @param gardenerID The gardener ID
     * @return List of garden IDs the gardener owns
     */
    public List<Integer> getOwnedGardens(int gardenerID) {
        if (!isGardener(gardenerID)) {
            return new ArrayList<>();
        }
        return gardenDAO.getOwnedGardenIDs(gardenerID);
    }
    
    /**
     * Get all garden IDs a gardener manages (but does not own).
     * 
     * @param gardenerID The gardener ID
     * @return List of garden IDs the gardener manages (non-owner)
     */
    public List<Integer> getManagedGardensOnly(int gardenerID) {
        if (!isGardener(gardenerID)) {
            return new ArrayList<>();
        }
        return gardenDAO.getManagedGardenIDs(gardenerID);
    }
    
    /**
     * Check if a gardener owns a specific garden.
     * 
     * @param gardenerID The gardener ID
     * @param gardenID The garden ID
     * @return True if the gardener owns this garden, false otherwise
     */
    public boolean isGardenOwner(int gardenerID, int gardenID) {
        if (!isGardener(gardenerID)) {
            return false;
        }
        return gardenDAO.isOwner(gardenID, gardenerID);
    }
    
    /**
     * Get all plants in a specific garden box.
     * 
     * @param gardenerID The gardener ID (for permission check)
     * @param gardenID The garden ID
     * @param boxID The box ID
     * @return List of plants in that box
     */
    public List<Plant> getPlantsInBox(int gardenerID, int gardenID, int boxID) {
        List<Plant> plants = new ArrayList<>();
        
        if (!isGardener(gardenerID)) {
            System.out.println("User " + gardenerID + " is not a gardener.");
            return plants;
        }
        
        if (!gardenDAO.canManageGarden(gardenID, gardenerID)) {
            System.out.println("Gardener " + gardenerID + " does not have access to garden " + gardenID);
            return plants;
        }
        
        String sql = "SELECT p.Plant_ID, p.Scientific_Name, p.Common_Name, p.Growth_Instructions, p.Soil_Type " +
                     "FROM PLANT p " +
                     "JOIN PLANTED_IN pi ON p.Plant_ID = pi.Plant_ID " +
                     "WHERE pi.Garden_ID = ? AND pi.Box_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, boxID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Plant plant = new Plant(
                    rs.getInt("Plant_ID"),
                    rs.getString("Scientific_Name"),
                    rs.getString("Common_Name"),
                    rs.getString("Growth_Instructions"),
                    rs.getString("Soil_Type")
                );
                plants.add(plant);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plants;
    }
    
    /**
     * Assign a task to a gardener.
     * 
     * @param gardenerID The ID of the gardener
     * @param taskID The ID of the task
     * @param plantID The plant ID associated with the task
     * @return True if assignment successful, false otherwise
     */
    public boolean assignTaskToGardener(int gardenerID, int taskID, int plantID) {
        if (!isGardener(gardenerID)) {
            System.out.println("User " + gardenerID + " is not a gardener.");
            return false;
        }
        
        return taskDAO.assignTask(taskID, plantID, gardenerID);
    }
}
