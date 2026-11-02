package com.gardenthyme.tests;

import java.sql.*;
import java.util.*;
import com.gardenthyme.models.HarvestLog;
import com.gardenthyme.dao.HarvestLogDAO;

public class HarvestLogDAOTest {
    public static void main(String[] args) {
        // Change "nethanya" to your own MySQL root password before running
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/GardenDB", "root", "nethanya")) {

            HarvestLogDAO dao = new HarvestLogDAO();

            // Add a new harvest log (Plant_ID 1 already exists in DB)
            HarvestLog log = new HarvestLog(4, 1, new java.util.Date(), 15);
            dao.addLog(log, conn);
            System.out.println("Inserted Harvest Log ID: " + log.getLogID());

            // Fetch all logs for plant 1
            List<HarvestLog> logs = HarvestLogDAO.getLogs(1, conn);
            System.out.println("Fetched " + logs.size() + " logs for Plant ID 1:");
            for (HarvestLog l : logs) {
                System.out.println("  Log ID: " + l.getLogID() + ", Quantity: " + l.getQuantity() + ", Date: " + l.getDate());
            }

            // Update the log
            log.setQuantity(20);
            log.setDate(new java.util.Date());
            dao.updateLog(log, conn);
            System.out.println("Updated Harvest Log");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}