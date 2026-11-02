package com.gardenthyme.gui.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gardenthyme.dao.GardenDAO;
import com.gardenthyme.dao.GardenerDAO;
import com.gardenthyme.dao.TaskDAO;
import com.gardenthyme.database.DatabaseConnection;
import com.gardenthyme.models.Garden;
import com.gardenthyme.models.Plant;
import com.gardenthyme.models.Task;

/**
 * GardenDataLoader handles all data retrieval and loading operations for garden-related views.
 * 
 * This utility class provides methods to:
 * - Load user gardens and their associated data
 * - Create new gardens and garden boxes
 * - Retrieve plants in specific garden boxes
 * - Get garden statistics (plant counts, pending tasks)
 * - Manage garden box information and soil types
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class GardenDataLoader {
    
    private static Connection connection;
    
    public static void initializeConnection() {
        connection = DatabaseConnection.getConnection();
        if (connection != null) {
            System.out.println("GardenDataLoader: Connection established");
            testDirectQuery();
        } else {
            System.err.println("GardenDataLoader: Failed to get connection!");
        }
    }
    
    private static Connection getFreshConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DatabaseConnection.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            connection = DatabaseConnection.getConnection();
        }
        return connection;
    }
    
    private static void testDirectQuery() {
        try {
            Connection conn = getFreshConnection();
            if (conn == null) return;
            String sql = "SELECT COUNT(*) as count FROM MANAGES";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("MANAGES table has " + rs.getInt("count") + " records");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Test query failed!");
            e.printStackTrace();
        }
    }
    
    public static boolean ensureSoilTypeExists(String soilType) {
        if (soilType == null || soilType.trim().isEmpty()) {
            return false;
        }
        
        Connection conn = getFreshConnection();
        if (conn == null) return false;
        
        try {
            String checkSql = "SELECT Soil_Type FROM SOIL WHERE Soil_Type = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, soilType);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    // Soil type doesn't exist, insert with default pH
                    String insertSql = "INSERT INTO SOIL (Soil_Type, pH) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, soilType);
                        insertStmt.setFloat(2, 6.5f);
                        insertStmt.executeUpdate();
                        System.out.println("Added new soil type: " + soilType + " with default pH 6.5");
                    }
                }
                rs.close();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static List<Garden> getUserGardens(int userID) {
        List<Garden> gardens = new ArrayList<>();
        
        Connection conn = getFreshConnection();
        if (conn == null) {
            System.err.println("Connection is null in getUserGardens!");
            return gardens;
        }
        
        GardenDAO gardenDAO = new GardenDAO(conn);
        List<Integer> gardenIDs = gardenDAO.getGardenIDsByUser(userID);
        System.out.println("Found " + gardenIDs.size() + " gardens for user " + userID);
        
        for (int gardenID : gardenIDs) {
            Garden garden = gardenDAO.getGardenByID(gardenID);
            if (garden != null) {
                garden.setOwner(gardenDAO.getOwner(gardenID) == userID);
                gardens.add(garden);
                System.out.println("  Added garden: " + garden.getGardenName() + " (ID: " + gardenID + ")");
            }
        }
        return gardens;
    }
    
    public static boolean createGarden(String gardenName, int ownerID) {
        try {
            Connection conn = getFreshConnection();
            if (conn == null) return false;
            
            int nextGardenID = getNextGardenID(conn);
            GardenDAO gardenDAO = new GardenDAO(conn);
            if (!gardenDAO.addGarden(nextGardenID, gardenName)) {
                return false;
            }
            if (!gardenDAO.assignOwner(nextGardenID, ownerID)) {
                return false;
            }
            GardenerDAO gardenerDAO = new GardenerDAO(conn);
            if (!gardenerDAO.isGardener(ownerID)) {
                gardenerDAO.addGardener(ownerID);
            }
            System.out.println("Created garden '" + gardenName + "' with ID: " + nextGardenID);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean addGardenBox(int gardenID, int boxID, float area, String soilType) {
        Connection conn = getFreshConnection();
        if (conn == null) return false;
        
        // Ensure soil type exists before adding box
        ensureSoilTypeExists(soilType);
        
        String sql = "INSERT INTO GARDEN_BOX (Garden_ID, Box_ID, Area, Soil_Type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, boxID);
            pstmt.setFloat(3, area);
            pstmt.setString(4, soilType);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Added garden box " + boxID + " to garden " + gardenID + " with soil type: " + soilType);
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean deleteGarden(int gardenID, int userID) {
        Connection conn = getFreshConnection();
        if (conn == null) return false;
        
        try {
            GardenDAO gardenDAO = new GardenDAO(conn);
            int ownerID = gardenDAO.getOwner(gardenID);
            if (ownerID != userID) {
                System.err.println("User " + userID + " is not the owner of garden " + gardenID);
                return false;
            }

            String deletePlantsSql = "DELETE FROM PLANTED_IN WHERE Garden_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deletePlantsSql)) {
                pstmt.setInt(1, gardenID);
                pstmt.executeUpdate();
            }

            String deleteBoxesSql = "DELETE FROM GARDEN_BOX WHERE Garden_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteBoxesSql)) {
                pstmt.setInt(1, gardenID);
                pstmt.executeUpdate();
            }
            
            String deleteManagesSql = "DELETE FROM MANAGES WHERE Garden_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteManagesSql)) {
                pstmt.setInt(1, gardenID);
                pstmt.executeUpdate();
            }
            
            String deleteGardenSql = "DELETE FROM GARDEN WHERE Garden_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteGardenSql)) {
                pstmt.setInt(1, gardenID);
                int affected = pstmt.executeUpdate();
                System.out.println("Deleted garden " + gardenID + ", rows affected: " + affected);
                return affected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean leaveGarden(int gardenID, int userID) {
        Connection conn = getFreshConnection();
        if (conn == null) return false;
        
        try {
            GardenDAO gardenDAO = new GardenDAO(conn);
            int ownerID = gardenDAO.getOwner(gardenID);
            if (ownerID == userID) {
                System.err.println("User " + userID + " is the owner, cannot leave. Use delete instead.");
                return false;
            }
            
            String deleteManagesSql = "DELETE FROM MANAGES WHERE Garden_ID = ? AND User_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteManagesSql)) {
                pstmt.setInt(1, gardenID);
                pstmt.setInt(2, userID);
                int affected = pstmt.executeUpdate();
                System.out.println("User " + userID + " left garden " + gardenID + ", rows affected: " + affected);
                return affected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean removePlantFromGardenBox(int plantID, int gardenID, int boxID) {
        Connection conn = getFreshConnection();
        if (conn == null) return false;
        
        String sql = "DELETE FROM PLANTED_IN WHERE Plant_ID = ? AND Garden_ID = ? AND Box_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, plantID);
            pstmt.setInt(2, gardenID);
            pstmt.setInt(3, boxID);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Removed plant " + plantID + " from garden " + gardenID + " box " + boxID);
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean addPlantToGardenBox(int plantID, int gardenID, int boxID) {
        Connection conn = getFreshConnection();
        if (conn == null) {
            System.err.println("Connection is null in addPlantToGardenBox!");
            return false;
        }
        
        // Check if the plant already exists in this box
        String checkSql = "SELECT * FROM PLANTED_IN WHERE Plant_ID = ? AND Garden_ID = ? AND Box_ID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, plantID);
            checkStmt.setInt(2, gardenID);
            checkStmt.setInt(3, boxID);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("Plant " + plantID + " already exists in garden " + gardenID + " box " + boxID);
                rs.close();
                return false;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        String sql = "INSERT INTO PLANTED_IN (Plant_ID, Garden_ID, Box_ID) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, plantID);
            pstmt.setInt(2, gardenID);
            pstmt.setInt(3, boxID);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Added plant " + plantID + " to garden " + gardenID + " box " + boxID);
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isPlantInBox(int plantID, int gardenID, int boxID) {
        Connection conn = getFreshConnection();
        if (conn == null) return false;
        
        String sql = "SELECT * FROM PLANTED_IN WHERE Plant_ID = ? AND Garden_ID = ? AND Box_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, plantID);
            pstmt.setInt(2, gardenID);
            pstmt.setInt(3, boxID);
            ResultSet rs = pstmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static int getNextBoxID(int gardenID) {
        List<Integer> boxes = getGardenBoxes(gardenID);
        if (boxes.isEmpty()) return 1;
        return Collections.max(boxes) + 1;
    }
    
    private static int getNextGardenID(Connection conn) {
        String sql = "SELECT MAX(Garden_ID) as max_id FROM GARDEN";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("max_id") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 5;
    }
    
    public static Map<Integer, List<Plant>> getPlantsInGardenBoxes(int gardenID) {
        Map<Integer, List<Plant>> boxPlants = new HashMap<>();
        
        Connection conn = getFreshConnection();
        if (conn == null) return boxPlants;
        
        String sql = "SELECT pi.Box_ID, p.Plant_ID, p.Scientific_Name, p.Common_Name, " +
                     "COUNT(CASE WHEN t.isCompleted = FALSE THEN 1 END) as pending_tasks " +
                     "FROM GARDEN_BOX gb " +
                     "LEFT JOIN PLANTED_IN pi ON gb.Garden_ID = pi.Garden_ID AND gb.Box_ID = pi.Box_ID " +
                     "LEFT JOIN PLANT p ON pi.Plant_ID = p.Plant_ID " +
                     "LEFT JOIN TASK t ON p.Plant_ID = t.Plant_ID " +
                     "WHERE gb.Garden_ID = ? " +
                     "GROUP BY pi.Box_ID, p.Plant_ID, p.Common_Name, p.Scientific_Name " +
                     "ORDER BY pi.Box_ID";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int boxID = rs.getInt("Box_ID");
                int plantID = rs.getInt("Plant_ID");
                
                if (plantID == 0) continue;
                
                Plant plant = new Plant(
                    plantID,
                    rs.getString("Scientific_Name") != null ? rs.getString("Scientific_Name") : "",
                    rs.getString("Common_Name") != null ? rs.getString("Common_Name") : "",
                    "",
                    ""
                );
                plant.setPendingTasks(rs.getInt("pending_tasks"));
                
                boxPlants.computeIfAbsent(boxID, k -> new ArrayList<>()).add(plant);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return boxPlants;
    }
    
    public static List<Integer> getGardenBoxes(int gardenID) {
        List<Integer> boxes = new ArrayList<>();
        
        Connection conn = getFreshConnection();
        if (conn == null) return boxes;
        
        String sql = "SELECT Box_ID FROM GARDEN_BOX WHERE Garden_ID = ? ORDER BY Box_ID";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                boxes.add(rs.getInt("Box_ID"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return boxes;
    }

    public static float getGardenBoxArea(int boxID, int gardenID) {
        Connection conn = getFreshConnection();
        if (conn == null) return 0;
        float area = 0;

        String sql = "SELECT Area FROM GARDEN_BOX WHERE Garden_ID = ? AND Box_ID = ? ORDER BY Box_ID";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, boxID);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            area = rs.getFloat("Area");
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return area;
    }

    public static float getGardenBoxPH(int boxID, int gardenID) {
        Connection conn = getFreshConnection();
        if (conn == null) return 0;
        float ph = 0;

        String sql = "SELECT pH FROM SOIL WHERE Soil_Type = (SELECT Soil_Type FROM GARDEN_BOX WHERE Box_ID = ? AND Garden_ID = ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, boxID);
            pstmt.setInt(2, gardenID);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            ph = rs.getFloat("pH");
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ph;
    }
    
    public static String getGardenOwnerName(int gardenID) {
        Connection conn = getFreshConnection();
        if (conn == null) return "Unknown";
        
        String sql = "SELECT u.Username FROM USERS u " +
                     "JOIN MANAGES m ON u.User_ID = m.User_ID " +
                     "WHERE m.Garden_ID = ? AND m.IsOwner = TRUE";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("Username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
    
    public static int getPlantCountInGarden(int gardenID) {
        Connection conn = getFreshConnection();
        if (conn == null) return 0;
        
        String sql = "SELECT COUNT(*) as count FROM PLANTED_IN WHERE Garden_ID = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static List<Task> getPendingTasksForPlant(int plantID) {
        Connection conn = getFreshConnection();
        if (conn == null) return new ArrayList<>();
        TaskDAO taskDAO = new TaskDAO(conn);
        return taskDAO.getTasksByPlant(plantID);
    }
    
    public static List<String> getSoilTypes() {
        List<String> soilTypes = new ArrayList<>();
        
        Connection conn = getFreshConnection();
        if (conn == null) return soilTypes;
        
        String sql = "SELECT Soil_Type FROM SOIL";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                soilTypes.add(rs.getString("Soil_Type"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return soilTypes;
    }
    
    public static boolean isGardenOwner(int gardenID, int userID) {
        Connection conn = getFreshConnection();
        if (conn == null) return false;
        
        GardenDAO gardenDAO = new GardenDAO(conn);
        return gardenDAO.getOwner(gardenID) == userID;
    }
}
