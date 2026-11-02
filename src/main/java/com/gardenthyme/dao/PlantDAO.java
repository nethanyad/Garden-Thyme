package com.gardenthyme.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gardenthyme.models.Plant;

public class PlantDAO {

    // Add a new plant (Plant_ID auto-generated)
   public void addPlant(Plant plant, Connection conn) throws SQLException {
        // First, ensure the soil type exists in SOIL table
        ensureSoilTypeExists(plant.getSoilType(), conn);
        
    String sql = "INSERT INTO PLANT (Scientific_Name, Common_Name, Growth_Instructions, Soil_Type) VALUES (?, ?, ?, ?)";

    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        stmt.setString(1, plant.getScientificName());
        stmt.setString(2, plant.getCommonName());
        stmt.setString(3, plant.getGrowthInstructions());
        stmt.setString(4, plant.getSoilType());

        int rows = stmt.executeUpdate();

        if (rows == 0) {
            throw new SQLException("Plant insert failed - no rows affected");
        }

        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                plant.setPlantID(rs.getInt(1));
            } else {
                throw new SQLException("Plant insert failed - no ID returned");
            }
        }
    }
}
    

    // Ensure a soil type exists in the SOIL table
    private void ensureSoilTypeExists(String soilType, Connection conn) throws SQLException {
        if (soilType == null || soilType.trim().isEmpty()) {
            return;
        }
        
        // Check if soil type already exists
        String checkSql = "SELECT Soil_Type FROM SOIL WHERE Soil_Type = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, soilType);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                // Soil type doesn't exist, insert it with a default pH value
                String insertSql = "INSERT INTO SOIL (Soil_Type, pH) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, soilType);
                    insertStmt.setFloat(2, 6.5f); // Default pH value
                    insertStmt.executeUpdate();
                    System.out.println("Added new soil type: " + soilType + " with default pH 6.5");
                }
            }
            rs.close();
        }
    }

    // Update an existing plant
    public void updatePlant(Plant plant, Connection conn) throws SQLException {
        // Ensure the new soil type exists
        ensureSoilTypeExists(plant.getSoilType(), conn);
        
        String sql = "UPDATE PLANT SET Scientific_Name=?, Common_Name=?, Growth_Instructions=?, Soil_Type=? WHERE Plant_ID=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, plant.getScientificName());
            stmt.setString(2, plant.getCommonName());
            stmt.setString(3, plant.getGrowthInstructions());
            stmt.setString(4, plant.getSoilType());
            stmt.setInt(5, plant.getPlantID());
            stmt.executeUpdate();
        }
    }

    // Delete a plant by ID
    public void deletePlant(int plantID, Connection conn) throws SQLException {
        String sql = "DELETE FROM PLANT WHERE Plant_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, plantID);
            stmt.executeUpdate();
        }
    }

    // Get a plant by ID
    public Plant getPlantByID(int plantID, Connection conn) throws SQLException {
        String sql = "SELECT * FROM PLANT WHERE Plant_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, plantID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Plant(
                        rs.getInt("Plant_ID"),
                        rs.getString("Scientific_Name"),
                        rs.getString("Common_Name"),
                        rs.getString("Growth_Instructions"),
                        rs.getString("Soil_Type")
                    );
                }
            }
        }
        return null;
    }

    // Get all plants
    public List<Plant> getAllPlants(Connection conn) throws SQLException {
        List<Plant> plants = new ArrayList<>();
        String sql = "SELECT * FROM PLANT";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                plants.add(new Plant(
                    rs.getInt("Plant_ID"),
                    rs.getString("Scientific_Name"),
                    rs.getString("Common_Name"),
                    rs.getString("Growth_Instructions"),
                    rs.getString("Soil_Type")
                ));
            }
        }
        return plants;
    }
}
