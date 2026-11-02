package com.gardenthyme.models;

public class Weather {

    private int plantID;
    private String bestMonth;
    private float temperature;
    private float humidity;
    private String sunlight;

    public Weather(int plantID, String bestMonth, float temperature, float humidity, String sunlight) {
        this.plantID = plantID;
        this.bestMonth = bestMonth;
        this.temperature = temperature;
        this.humidity = humidity;
        this.sunlight = sunlight;
    }

    public int getPlantID() { return plantID; }
    public void setPlantID(int plantID) { this.plantID = plantID; }

    public String getBestMonth() { return bestMonth; }
    public void setBestMonth(String bestMonth) { this.bestMonth = bestMonth; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public float getHumidity() { return humidity; }
    public void setHumidity(float humidity) { this.humidity = humidity; }

    public String getSunlight() { return sunlight; }
    public void setSunlight(String sunlight) { this.sunlight = sunlight; }
}