package com.gardenthyme.tests;

import java.sql.*;
import java.util.Arrays;
import com.gardenthyme.models.Pest;
import com.gardenthyme.dao.PestDAO;

public class PestDAOTest {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/GardenDB", "root", "nethanya")) // Change "nethanya" to your own MySQL root password before running
        {
            PestDAO dao = new PestDAO();

            // Add a new pest
            Pest aphid = new Pest(
                    "Aphid",
                    "Neem Oil, Insecticidal Soap",
                    Arrays.asList("Tomato", "Rose", "Pepper"),
                    Arrays.asList("Sticky residue on leaves", "Curled or yellowing leaves"));
            dao.addPest(aphid, conn);
            System.out.println("Inserted Pest: " + aphid.getPestName());

            // Fetch the pest
            Pest fetched = dao.getPestByID("Aphid", conn);
            if (fetched != null) {
                System.out.println("Fetched Pest: " + fetched.getPestName());
                System.out.println("Pesticides: " + fetched.getEffectivePesticides());
                System.out.println("Target Plants: " + fetched.getTargetPlants());
                System.out.println("Signs: " + fetched.getSigns());

                // Update the pest
                fetched.setEffectivePesticides("Pyrethrin, Neem Oil");
                fetched.setTargetPlants(Arrays.asList("Tomato", "Cabbage"));
                fetched.setSigns(Arrays.asList("White residue on stems", "Wilting leaves"));
                dao.updatePest(fetched, conn);
                System.out.println("Updated Pest");

                // Delete the pest
                dao.deletePest(fetched.getPestName(), conn);
                System.out.println("Deleted Pest");
            } else {
                System.out.println("Pest not found");
            }

            // Re-add Aphid so we can test linking (was deleted above)
            dao.addPest(aphid, conn);

            // Test linking
            dao.linkPestToPlant("Aphid", 1, conn);
            System.out.println("Linked Aphid to Plant 1");

            // Test get plants by pest
            System.out.println("Plants infected by Aphid: " + dao.getPlantsByPest("Aphid", conn));

            // Test get pests by plant
            System.out.println("Pests infecting Plant 1: " + dao.getPestsByPlant(1, conn));

            // Test unlinking
            dao.unlinkPestFromPlant("Aphid", 1, conn);
            System.out.println("Unlinked Aphid from Plant 1");

            // Cleanup
            dao.deletePest("Aphid", conn);
            System.out.println("Cleaned up Aphid");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}