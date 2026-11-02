package com.gardenthyme.dao;

import java.sql.*;
import com.gardenthyme.models.Soil;

public class SoilDAO {

    public void addSoil(Soil soil, Connection conn) throws SQLException {
        String sql = "INSERT INTO SOIL (Soil_Type, pH) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soil.getSoilType());
            stmt.setFloat(2, soil.getPH());
            stmt.executeUpdate();
        }
    }

   
    public void updateSoil(Soil soil, Connection conn) throws SQLException {
        String sql = "UPDATE SOIL SET pH=? WHERE Soil_Type=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setFloat(1, soil.getPH());
            stmt.setString(2, soil.getSoilType());
            stmt.executeUpdate();
        }
    }

    
    public void deleteSoil(String soilType, Connection conn) throws SQLException {
        String sql = "DELETE FROM SOIL WHERE Soil_Type=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soilType);
            stmt.executeUpdate();
        }
    }

    
    public Soil getSoilByType(String soilType, Connection conn) throws SQLException {
        String sql = "SELECT * FROM SOIL WHERE Soil_Type=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soilType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Soil(
                        rs.getString("Soil_Type"),
                        rs.getFloat("pH")
                    );
                }
            }
        }
        return null;
    }
}