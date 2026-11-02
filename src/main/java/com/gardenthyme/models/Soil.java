package com.gardenthyme.models;

public class Soil {
    private String soilType;
    private float pH;

    // Constructor
    public Soil(String soilType, float pH) {
        this.soilType = soilType;
        this.pH = pH;
    }

    // Getters
    public String getSoilType() { return soilType; }
    public float getPH() { return pH; }

    // Setters
    public void setSoilType(String soilType) { this.soilType = soilType; }
    public void setPH(float pH) { this.pH = pH; }

    // Returns a formatted string describing the soil
    public String getSoilInfo() {
        return soilType + ", pH " + pH;
    }
}