package com.gardenthyme.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gardenthyme.models.GardenBox;
import com.gardenthyme.models.Task;

/**
 * GardenBoxDAO handles all database operations for the GARDEN_BOX table.
 * - addBox()
 * - viewBoxInfo()
 * - deleteBox()
 * - addTaskToAllPlants()
 * 
 * Note: GardenBox has a composite primary key (Garden_ID, Box_ID).
 * Most methods require both gardenID and boxID.
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class GardenBoxDAO {
    
    private Connection connection;
    private TaskDAO taskDAO;
    
    public GardenBoxDAO(Connection connection) {
        this.connection = connection;
        this.taskDAO = new TaskDAO(connection);
    }

    /**
     * Add a new garden box to a garden.
     * 
     * @param gardenID The ID of the garden this box belongs to
     * @param boxID The ID of the box (unique within the garden)
     * @param area The area of the box in square feet
     * @param soilType The soil type in this box (foreign key to SOIL table)
     * @return True if addition successful, false otherwise
     */
    public boolean addBox(int gardenID, int boxID, float area, String soilType) {
        // Check if the garden exists
        String checkGardenSql = "SELECT * FROM GARDEN WHERE Garden_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkGardenSql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Garden " + gardenID + " does not exist.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Check if soil type exists
        String checkSoilSql = "SELECT * FROM SOIL WHERE Soil_Type = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSoilSql)) {
            pstmt.setString(1, soilType);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Soil type '" + soilType + "' does not exist.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Check if box already exists in this garden
        String checkBoxSql = "SELECT * FROM GARDEN_BOX WHERE Garden_ID = ? AND Box_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkBoxSql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, boxID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Box " + boxID + " already exists in garden " + gardenID);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Add the box
        String sql = "INSERT INTO GARDEN_BOX (Garden_ID, Box_ID, Area, Soil_Type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, boxID);
            pstmt.setFloat(3, area);
            pstmt.setString(4, soilType);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * View information about a specific garden box.
     * 
     * @param gardenID The ID of the garden
     * @param boxID The ID of the box
     * @return GardenBox object with box information, or null if not found
     */
    public GardenBox viewBoxInfo(int gardenID, int boxID) {
        String sql = "SELECT Box_ID, Area FROM GARDEN_BOX WHERE Garden_ID = ? AND Box_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, boxID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new GardenBox(
                    rs.getInt("Box_ID"),
                    rs.getFloat("Area")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Delete a garden box.
     * 
     * @param gardenID The ID of the garden
     * @param boxID The ID of the box to delete
     * @return True if deletion successful, false otherwise
     */
    public boolean deleteBox(int gardenID, int boxID) {
        // Check if the box exists
        String checkSql = "SELECT * FROM GARDEN_BOX WHERE Garden_ID = ? AND Box_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, boxID);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Box " + boxID + " does not exist in garden " + gardenID);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Delete the box (PLANTED_IN records will be deleted due to CASCADE)
        String sql = "DELETE FROM GARDEN_BOX WHERE Garden_ID = ? AND Box_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, boxID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add a task to all plants in a garden box.
     * 
     * @param gardenID The ID of the garden
     * @param boxID The ID of the box
     * @param taskText The description of the task to add
     * @param taskDate The date the task should be performed
     * @param frequency How often the task repeats
     * @param repeatUntil Date until which the task repeats (null for no end date)
     * @return Number of tasks added (or -1 if error)
     */
    public int addTaskToAllPlants(int gardenID, int boxID, String taskText, 
                                   Date taskDate, int frequency, Date repeatUntil) {
        // Get all plants in this box
        List<Integer> plantIDs = getPlantsInBox(gardenID, boxID);
        
        if (plantIDs.isEmpty()) {
            System.out.println("No plants found in box " + boxID + " of garden " + gardenID);
            return 0;
        }
        
        int tasksAdded = 0;
        
        // For each plant, create a task
        for (int plantID : plantIDs) {
            // Get the next available task ID for this plant
            int nextTaskID = getNextTaskIDForPlant(plantID);
            
            Task task = new Task();
            task.setTaskID(nextTaskID);
            task.setPlantID(plantID);
            task.setDate(taskDate);
            task.setText(taskText);
            task.setCompleted(false);
            task.setFrequency(frequency);
            task.setRepeatUntil(repeatUntil);
            
            if (taskDAO.addTask(task)) {
                tasksAdded++;
            }
        }
        
        return tasksAdded;
    }
    
    // ===== Additional Helper Methods =====
    
    /**
     * Get all plants in a specific garden box.
     * Helper method for addTaskToAllPlants().
     * 
     * @param gardenID The ID of the garden
     * @param boxID The ID of the box
     * @return List of plant IDs in the box
     */
    private List<Integer> getPlantsInBox(int gardenID, int boxID) {
        List<Integer> plantIDs = new ArrayList<>();
        String sql = "SELECT Plant_ID FROM PLANTED_IN WHERE Garden_ID = ? AND Box_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, boxID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                plantIDs.add(rs.getInt("Plant_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plantIDs;
    }
    
    /**
     * Get the next available task ID for a specific plant.
     * Helper method for addTaskToAllPlants().
     * 
     * @param plantID The plant ID
     * @return The next available task ID (max existing ID + 1, or 1 if no tasks exist)
     */
    private int getNextTaskIDForPlant(int plantID) {
        String sql = "SELECT MAX(Task_ID) FROM TASK WHERE Plant_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, plantID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // First task for this plant
    }
    
    /**
     * Get all boxes in a specific garden.
     * 
     * @param gardenID The ID of the garden
     * @return List of GardenBox objects in the garden
     */
    public List<GardenBox> getBoxesByGarden(int gardenID) {
        List<GardenBox> boxes = new ArrayList<>();
        String sql = "SELECT Box_ID, Area FROM GARDEN_BOX WHERE Garden_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                GardenBox box = new GardenBox(
                    rs.getInt("Box_ID"),
                    rs.getFloat("Area")
                );
                boxes.add(box);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return boxes;
    }
    
    /**
     * Update the soil type of a garden box.
     * 
     * @param gardenID The ID of the garden
     * @param boxID The ID of the box
     * @param soilType The new soil type
     * @return True if update successful, false otherwise
     */
    public boolean updateSoilType(int gardenID, int boxID, String soilType) {
        String sql = "UPDATE GARDEN_BOX SET Soil_Type = ? WHERE Garden_ID = ? AND Box_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, soilType);
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
     * Update the area of a garden box.
     * 
     * @param gardenID The ID of the garden
     * @param boxID The ID of the box
     * @param area The new area
     * @return True if update successful, false otherwise
     */
    public boolean updateArea(int gardenID, int boxID, float area) {
        String sql = "UPDATE GARDEN_BOX SET Area = ? WHERE Garden_ID = ? AND Box_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setFloat(1, area);
            pstmt.setInt(2, gardenID);
            pstmt.setInt(3, boxID);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
