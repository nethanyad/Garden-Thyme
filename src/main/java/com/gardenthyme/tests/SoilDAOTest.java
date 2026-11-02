package com.gardenthyme.tests;

import java.sql.*;
import com.gardenthyme.models.Soil;
import com.gardenthyme.dao.SoilDAO;

public class SoilDAOTest {
    public static void main(String[] args) {
        
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/GardenDB", "root", "nethanya")) { // Change "nethanya" to your own MySQL root password before running 
            SoilDAO dao = new SoilDAO();

            // Add a new soil
            Soil soil = new Soil("Silt", 6.8f);
            dao.addSoil(soil, conn);
            System.out.println("Inserted Soil: " + soil.getSoilInfo());

            // Fetch the soil
            Soil fetched = dao.getSoilByType("Silt", conn);
            if (fetched != null) {
                System.out.println("Fetched Soil: " + fetched.getSoilInfo());

                // Update the soil
                fetched.setPH(7.0f);
                dao.updateSoil(fetched, conn);
                System.out.println("Updated Soil pH");

                // Delete the soil
                dao.deleteSoil(fetched.getSoilType(), conn);
                System.out.println("Deleted Soil");
            } else {
                System.out.println("Soil not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}