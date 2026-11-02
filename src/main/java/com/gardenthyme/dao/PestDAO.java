package com.gardenthyme.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gardenthyme.models.Pest;

public class PestDAO {

    public void addPest(Pest pest, Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            String insertPest = "INSERT INTO PESTS (Name, Effective_Pesticides) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertPest)) {
                ps.setString(1, pest.getPestName());
                ps.setString(2, pest.getEffectivePesticides());
                ps.executeUpdate();
            }

            if (pest.getTargetPlants() != null) {
                String insertPlant = "INSERT INTO TARGET_PLANTS (Pest_Name, Pest_Target_Plants) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertPlant)) {
                    for (String plant : pest.getTargetPlants()) {
                        ps.setString(1, pest.getPestName());
                        ps.setString(2, plant);
                        ps.executeUpdate();
                    }
                }
            }

            if (pest.getSigns() != null) {
                String insertSign = "INSERT INTO SIGNS (Pest_Name, Pest_Signs) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSign)) {
                    for (String sign : pest.getSigns()) {
                        ps.setString(1, pest.getPestName());
                        ps.setString(2, sign);
                        ps.executeUpdate();
                    }
                }
            }

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void updatePest(Pest pest, Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            String updatePest = "UPDATE PESTS SET Effective_Pesticides = ? WHERE Name = ?";
            try (PreparedStatement ps = conn.prepareStatement(updatePest)) {
                ps.setString(1, pest.getEffectivePesticides());
                ps.setString(2, pest.getPestName());
                ps.executeUpdate();
            }

            String deletePlants = "DELETE FROM TARGET_PLANTS WHERE Pest_Name = ?";
            try (PreparedStatement ps = conn.prepareStatement(deletePlants)) {
                ps.setString(1, pest.getPestName());
                ps.executeUpdate();
            }

            String deleteSigns = "DELETE FROM SIGNS WHERE Pest_Name = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSigns)) {
                ps.setString(1, pest.getPestName());
                ps.executeUpdate();
            }

            if (pest.getTargetPlants() != null) {
                String insertPlant = "INSERT INTO TARGET_PLANTS (Pest_Name, Pest_Target_Plants) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertPlant)) {
                    for (String plant : pest.getTargetPlants()) {
                        ps.setString(1, pest.getPestName());
                        ps.setString(2, plant);
                        ps.executeUpdate();
                    }
                }
            }

            if (pest.getSigns() != null) {
                String insertSign = "INSERT INTO SIGNS (Pest_Name, Pest_Signs) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSign)) {
                    for (String sign : pest.getSigns()) {
                        ps.setString(1, pest.getPestName());
                        ps.setString(2, sign);
                        ps.executeUpdate();
                    }
                }
            }

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void deletePest(String pestName, Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            String deleteLink = "DELETE FROM PEST_PLANT_LINK WHERE Pest_Name = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteLink)) {
                ps.setString(1, pestName);
                ps.executeUpdate();
            }

            String deletePlants = "DELETE FROM TARGET_PLANTS WHERE Pest_Name = ?";
            try (PreparedStatement ps = conn.prepareStatement(deletePlants)) {
                ps.setString(1, pestName);
                ps.executeUpdate();
            }

            String deleteSigns = "DELETE FROM SIGNS WHERE Pest_Name = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSigns)) {
                ps.setString(1, pestName);
                ps.executeUpdate();
            }

            String deletePest = "DELETE FROM PESTS WHERE Name = ?";
            try (PreparedStatement ps = conn.prepareStatement(deletePest)) {
                ps.setString(1, pestName);
                ps.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public Pest getPestByID(String pestName, Connection conn) throws SQLException {
        Pest pest = null;

        String queryPest = "SELECT Name, Effective_Pesticides FROM PESTS WHERE Name = ?";
        try (PreparedStatement ps = conn.prepareStatement(queryPest)) {
            ps.setString(1, pestName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pest = new Pest(rs.getString("Name"), rs.getString("Effective_Pesticides"));
                }
            }
        }

        if (pest == null) return null;

        List<String> targetPlants = new ArrayList<>();
        String queryPlants = "SELECT Pest_Target_Plants FROM TARGET_PLANTS WHERE Pest_Name = ?";
        try (PreparedStatement ps = conn.prepareStatement(queryPlants)) {
            ps.setString(1, pestName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    targetPlants.add(rs.getString("Pest_Target_Plants"));
                }
            }
        }

        List<String> signs = new ArrayList<>();
        String querySigns = "SELECT Pest_Signs FROM SIGNS WHERE Pest_Name = ?";
        try (PreparedStatement ps = conn.prepareStatement(querySigns)) {
            ps.setString(1, pestName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    signs.add(rs.getString("Pest_Signs"));
                }
            }
        }

        pest.setTargetPlants(targetPlants);
        pest.setSigns(signs);

        return pest;
    }

    /**
     * Link a pest to a plant using the PEST_PLANT_LINK table.
     */
    public void linkPestToPlant(String pestName, int plantID, Connection conn) throws SQLException {
        String sql = "INSERT INTO PEST_PLANT_LINK (Pest_Name, Plant_ID) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pestName);
            ps.setInt(2, plantID);
            ps.executeUpdate();
        }
    }

    /**
     * Unlink a pest from a plant.
     */
    public void unlinkPestFromPlant(String pestName, int plantID, Connection conn) throws SQLException {
        String sql = "DELETE FROM PEST_PLANT_LINK WHERE Pest_Name = ? AND Plant_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pestName);
            ps.setInt(2, plantID);
            ps.executeUpdate();
        }
    }

    /**
     * Get all plants linked to a specific pest.
     */
    public List<Integer> getPlantsByPest(String pestName, Connection conn) throws SQLException {
        List<Integer> plantIDs = new ArrayList<>();
        String query = "SELECT Plant_ID FROM PEST_PLANT_LINK WHERE Pest_Name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, pestName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    plantIDs.add(rs.getInt("Plant_ID"));
                }
            }
        }
        return plantIDs;
    }

    /**
     * Get all pests linked to a specific plant.
     */
    public List<String> getPestsByPlant(int plantID, Connection conn) throws SQLException {
        List<String> pestNames = new ArrayList<>();
        String query = "SELECT Pest_Name FROM PEST_PLANT_LINK WHERE Plant_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, plantID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pestNames.add(rs.getString("Pest_Name"));
                }
            }
        }
        return pestNames;
    }
    
    public List<Pest> getAllPests(Connection conn) throws SQLException {
        List<Pest> pests = new ArrayList<>();
        String query = "SELECT Name, Effective_Pesticides FROM PESTS";
        try (PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pests.add(new Pest(
                    rs.getString("Name"),
                    rs.getString("Effective_Pesticides")
                ));
            }
        }
        return pests;
    }
}
