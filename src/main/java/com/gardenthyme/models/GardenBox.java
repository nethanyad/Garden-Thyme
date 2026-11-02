package com.gardenthyme.models;

/**
 * GardenBox class representing individual planting boxes within a garden.
 * Maps to GARDEN_BOX table in the database with composite primary key (Garden_ID, Box_ID).
 * Each box belongs to one garden and can contain multiple plants.
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class GardenBox {
    // Unique box ID within a garden
    private int boxID;
    
    // Area of the box in square feet
    private float area;
    
    /**
     * Default constructor
     */
    public GardenBox() {}
    
    /**
     * Parameterized constructor
     * @param boxID Unique identifier for this box within its garden
     * @param area Area of the box in square feet
     */
    public GardenBox(int boxID, float area) {
        this.boxID = boxID;
        this.area = area;
    }
    
    // ===== Getters and Setters =====
    
    public int getBoxID() {
        return boxID;
    }
    
    public void setBoxID(int boxID) {
        this.boxID = boxID;
    }
    
    public float getArea() {
        return area;
    }
    
    public void setArea(float area) {
        this.area = area;
    }
    
    @Override
    public String toString() {
        return "GardenBox{boxID=" + boxID + ", area=" + area + "}";
    }
}
