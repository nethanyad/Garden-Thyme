package com.gardenthyme.dao;

import com.gardenthyme.models.Weather;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WeatherDAO {

    public void addWeatherData(Weather weather, Connection conn) throws SQLException {
        String sql = "INSERT INTO WEATHER_DATA (Plant_ID, Best_Month, Temperature, Humidity, Sunlight) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, weather.getPlantID());
            ps.setString(2, weather.getBestMonth());
            ps.setFloat(3, weather.getTemperature());
            ps.setFloat(4, weather.getHumidity());
            ps.setString(5, weather.getSunlight());
            ps.executeUpdate();
        }
    }

    public void updateWeatherData(Weather weather, Connection conn) throws SQLException {
        String sql = "UPDATE WEATHER_DATA SET Temperature = ?, Humidity = ?, Sunlight = ? WHERE Plant_ID = ? AND Best_Month = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, weather.getTemperature());
            ps.setFloat(2, weather.getHumidity());
            ps.setString(3, weather.getSunlight());
            ps.setInt(4, weather.getPlantID());
            ps.setString(5, weather.getBestMonth());
            ps.executeUpdate();
        }
    }

    public void deleteWeatherData(int plantID, String bestMonth, Connection conn) throws SQLException {
        String sql = "DELETE FROM WEATHER_DATA WHERE Plant_ID = ? AND Best_Month = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantID);
            ps.setString(2, bestMonth);
            ps.executeUpdate();
        }
    }

    public List<Weather> getWeatherInfoByPlant(int plantID, Connection conn) throws SQLException {
        List<Weather> results = new ArrayList<>();
        String sql = "SELECT * FROM WEATHER_DATA WHERE Plant_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new Weather(
                        rs.getInt("Plant_ID"),
                        rs.getString("Best_Month"),
                        rs.getFloat("Temperature"),
                        rs.getFloat("Humidity"),
                        rs.getString("Sunlight")
                    ));
                }
            }
        }
        return results;
    }
}