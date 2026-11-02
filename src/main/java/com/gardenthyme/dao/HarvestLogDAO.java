package com.gardenthyme.dao;

import java.sql.*;
import java.util.*;
import com.gardenthyme.models.HarvestLog;

public class HarvestLogDAO {

    // Insert a new harvest log
    public void addLog(HarvestLog log, Connection conn) throws SQLException {
        String sql = "INSERT INTO HARVEST_LOG (Plant_ID, Harvest_Date, Quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, log.getGardenPlantID());
            stmt.setDate(2, new java.sql.Date(log.getDate().getTime()));
            stmt.setInt(3, log.getQuantity());
            stmt.executeUpdate();
        }
    }

    // Update an existing harvest log
    public void updateLog(HarvestLog log, Connection conn) throws SQLException {
        String sql = "UPDATE HARVEST_LOG SET Harvest_Date=?, Quantity=? WHERE Plant_ID=? AND ID=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(log.getDate().getTime()));
            stmt.setInt(2, log.getQuantity());
            stmt.setInt(3, log.getGardenPlantID());
            stmt.setInt(4, log.getLogID());
            stmt.executeUpdate();
        }
    }

    // Fetch all harvest logs for a specific gardenPlantID
    public static List<HarvestLog> getLogs(int gardenPlantID, Connection conn) throws SQLException {
        String sql = "SELECT * FROM HARVEST_LOG WHERE Plant_ID=?";
        List<HarvestLog> logs = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gardenPlantID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new HarvestLog(
                        rs.getInt("ID"),
                        rs.getInt("Plant_ID"),
                        rs.getDate("Harvest_Date"),
                        rs.getInt("Quantity")
                    ));
                }
            }
        }
        return logs;
    }
}