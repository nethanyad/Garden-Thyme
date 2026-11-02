package com.gardenthyme.tests;

import java.sql.*;
import com.gardenthyme.models.Plant;
import com.gardenthyme.dao.PlantDAO;

public class PlantDAOTest {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/GardenDB", "root", "nethanya")) // Change "nethanya" to your own MySQL root password before running
            { 
            PlantDAO dao = new PlantDAO();

            // Add a new plant
            Plant tomato = new Plant("Solanum lycopersicum", "Tomato", "Water regularly", "Loam");
            dao.addPlant(tomato, conn);
            System.out.println("Inserted Plant ID: " + tomato.getPlantID());

            // Fetch the plant
            Plant fetched = dao.getPlantByID(tomato.getPlantID(), conn);
            if (fetched != null) {
                System.out.println("Fetched Plant: " + fetched.getCommonName());

                // Update the plant
                fetched.setGrowthInstructions("Water daily");
                dao.updatePlant(fetched, conn);
                System.out.println("Updated Plant Growth Instructions");

                // Delete the plant
                dao.deletePlant(fetched.getPlantID(), conn);
                System.out.println("Deleted Plant");
            } else {
                System.out.println("Plant not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}