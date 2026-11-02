package com.gardenthyme.models;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.List;

public class HarvestLog {
    private int logID;
    private int gardenPlantID;
    private Date date;
    private int quantity;

    // Constructor
    public HarvestLog(int logID, int gardenPlantID, Date date, int quantity) {
        this.logID = logID;
        this.gardenPlantID = gardenPlantID;
        this.date = date;
        this.quantity = quantity;
    }

    public HarvestLog(int gardenPlantID, Date date, int quantity) {
        this.gardenPlantID = gardenPlantID;
        this.date = date;
        this.quantity = quantity;
    }

    // Getters
    public int getLogID() { return logID; }
    public int getGardenPlantID() { return gardenPlantID; }
    public Date getDate() { return date; }
    public int getQuantity() { return quantity; }

    // Setters
    public void setLogID(int logID) { this.logID = logID; }
    public void setGardenPlantID(int gardenPlantID) { this.gardenPlantID = gardenPlantID; }
    public void setDate(Date date) { this.date = date; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}