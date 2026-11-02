package com.gardenthyme.models;

public class Plant {

    private int plantID;
    private String scientificName;
    private String commonName;
    private String growthInstructions;
    private String soilType;
    private int pendingTasks;

    // Constructor for creating a new plant (no ID yet)
    public Plant(String scientificName, String commonName, String growthInstructions, String soilType) {
        this.scientificName = scientificName;
        this.commonName = commonName;
        this.growthInstructions = growthInstructions;
        this.soilType = soilType;
    }

    // Constructor with ID (for fetching from DB)
    public Plant(int plantID, String scientificName, String commonName, String growthInstructions, String soilType) {
        this.plantID = plantID;
        this.scientificName = scientificName;
        this.commonName = commonName;
        this.growthInstructions = growthInstructions;
        this.soilType = soilType;
    }

    public int getPlantID() {
        return plantID;
    }

    public void setPlantID(int plantID) {
        this.plantID = plantID;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getGrowthInstructions() {
        return growthInstructions;
    }

    public void setGrowthInstructions(String growthInstructions) {
        this.growthInstructions = growthInstructions;
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

}
