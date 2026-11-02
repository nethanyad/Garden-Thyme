package com.gardenthyme.tests;

import java.sql.*;
import com.gardenthyme.models.Weather;
import com.gardenthyme.dao.WeatherDAO;

public class WeatherDAOTest {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/GardenDB", "root", "nethanya")) // Change "nethanya" to your own MySQL root password before running
        {
            WeatherDAO dao = new WeatherDAO();

            // Add weather data for Plant_ID 1
            Weather weather = new Weather(1, "August", 28.0f, 65.0f, "Full Sun");
            dao.addWeatherData(weather, conn);
            System.out.println("Inserted WeatherData for Plant 1 in August");

            // Fetch all weather for Plant 1
            System.out.println("Weather records for Plant 1:");
            for (Weather w : dao.getWeatherInfoByPlant(1, conn)) {
                System.out.println("  Month: " + w.getBestMonth() + 
                                   ", Temp: " + w.getTemperature() + 
                                   ", Humidity: " + w.getHumidity() + 
                                   ", Sunlight: " + w.getSunlight());
            }

            // Update the record
            weather.setTemperature(30.0f);
            weather.setHumidity(70.0f);
            dao.updateWeatherData(weather, conn);
            System.out.println("Updated Weather");

            // Delete the record
            dao.deleteWeatherData(1, "August", conn);
            System.out.println("Deleted Weather");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}