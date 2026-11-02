package com.gardenthyme.models;

/**
 * Garden class representing a garden in the system.
 * Maps to GARDEN table in the database.
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class Garden {
    // Attributes from GARDEN table
    private int gardenID;
    private String gardenName;
    private boolean isOwner;
    
    /**
     * Default constructor
     */
    public Garden() {}
    
    /**
     * Parameterized constructor
     * @param gardenID Unique identifier for the garden
     * @param gardenName Name of the garden
     */
    public Garden(int gardenID, String gardenName) {
        this.gardenID = gardenID;
        this.gardenName = gardenName;
    }
    
    // ===== Getters and Setters =====
    
    public int getGardenID() {
        return gardenID;
    }
    
    public void setGardenID(int gardenID) {
        this.gardenID = gardenID;
    }
    
    public String getGardenName() {
        return gardenName;
    }
    
    public void setGardenName(String gardenName) {
        this.gardenName = gardenName;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }
    
    @Override
    public String toString() {
        return "Garden{gardenID=" + gardenID + ", gardenName='" + gardenName + "'}";
    }
}
