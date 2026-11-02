package com.gardenthyme.models;

import java.sql.Date;

/**
 * Task class representing gardening tasks that need to be completed.
 * Tasks can be assigned to users (gardeners) and can be recurring.
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class Task {
    // Unique task ID (part of composite key with Plant_ID)
    private int taskID;
    
    // The plant this task is associated with (part of composite key with Task_ID)
    private int plantID;
    
    // Date when the task should be performed
    private Date date;
    
    // Description of the task (e.g., "Water tomato plants")
    private String text;
    
    // Whether the task has been completed
    private boolean isCompleted;
    
    // How often the task repeats (e.g., "Daily", "Weekly", "Monthly", "One-time")
    private int frequency;
    
    // Date until which the task should repeat (null if no end date)
    private Date repeatUntil;
    
    /**
     * Default constructor
     */
    public Task() {}
    
    /**
     * Parameterized constructor
     * @param taskID Unique identifier for the task
     * @param plantID The plant this task is associated with
     * @param date Date when task should be performed
     * @param text Description of the task
     * @param isCompleted Whether the task is completed
     * @param frequency How often the task repeats
     * @param repeatUntil Date until which task repeats (null for no end date)
     */
    public Task(int taskID, int plantID, Date date, String text, 
                boolean isCompleted, int frequency, Date repeatUntil) {
        this.taskID = taskID;
        this.plantID = plantID;
        this.date = date;
        this.text = text;
        this.isCompleted = isCompleted;
        this.frequency = frequency;
        this.repeatUntil = repeatUntil;
    }

    public Task(int plantID, Date date, String text,
                boolean isCompleted, int frequency, Date repeatUntil) {
        this.plantID = plantID;
        this.date = date;
        this.text = text;
        this.isCompleted = isCompleted;
        this.frequency = frequency;
        this.repeatUntil = repeatUntil;
    }
    
    // ===== Getters and Setters =====
    
    public int getTaskID() {
        return taskID;
    }
    
    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }
    
    public int getPlantID() {
        return plantID;
    }
    
    public void setPlantID(int plantID) {
        this.plantID = plantID;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    
    public int getFrequency() {
        return frequency;
    }
    
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    
    public Date getRepeatUntil() {
        return repeatUntil;
    }
    
    public void setRepeatUntil(Date repeatUntil) {
        this.repeatUntil = repeatUntil;
    }
    
    @Override
    public String toString() {
        return "Task{taskID=" + taskID + ", plantID=" + plantID + 
               ", text='" + text + "', isCompleted=" + isCompleted + "}";
    }
}
