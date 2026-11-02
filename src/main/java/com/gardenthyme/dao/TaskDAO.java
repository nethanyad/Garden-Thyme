package com.gardenthyme.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gardenthyme.models.Task;

/**
 * TaskDAO handles all database operations for the TASK table.
 * - assignTask()
 * - updateTask()
 * - completeTask()
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class TaskDAO {
    
    private Connection connection;
    
    public TaskDAO(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Assign a task to a user (gardener).
     * 
     * @param taskID The ID of the task
     * @param plantID The plant ID associated with the task
     * @param userID The user ID to assign the task to
     * @return True if assignment successful, false otherwise
     */
    public boolean assignTask(int taskID, int plantID, int userID) {
        String sql = "INSERT INTO ASSIGNED_TO (Plant_ID, Task_ID, User_ID) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, plantID);
            pstmt.setInt(2, taskID);
            pstmt.setInt(3, userID);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing task's details.
     * 
     * @param task The task object with updated information
     * @return True if update successful, false otherwise
     */
    public boolean updateTask(Task task) {
        String sql = "UPDATE TASK SET Date = ?, Text = ?, isCompleted = ?, " +
                     "Frequency = ?, Repeat_Until = ? WHERE Task_ID = ? AND Plant_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, task.getDate());
            pstmt.setString(2, task.getText());
            pstmt.setBoolean(3, task.isCompleted());
            pstmt.setInt(4, task.getFrequency());
            pstmt.setDate(5, task.getRepeatUntil());
            pstmt.setInt(6, task.getTaskID());
            pstmt.setInt(7, task.getPlantID());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Mark a task as completed.
     * 
     * @param taskID The ID of the task
     * @param plantID The plant ID associated with the task
     * @return True if completion successful, false otherwise
     */
    public boolean completeTask(int taskID, int plantID, boolean completion) {
        String sql = "UPDATE TASK SET isCompleted = TRUE WHERE Task_ID = ? AND Plant_ID = ?";
        if (!completion){
            sql = "UPDATE TASK SET isCompleted = FALSE WHERE Task_ID = ? AND Plant_ID = ?";
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, taskID);
            pstmt.setInt(2, plantID);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== Additional Helper Methods =====
    
    /**
     * Add a new task to the database.
     * 
     * @param task The task object to add
     * @return True if addition successful, false otherwise
     */
    public boolean addTask(Task task) {
        String sql = "INSERT INTO TASK (Plant_ID, Date, Text, isCompleted, Frequency, Repeat_Until) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, task.getPlantID());
            pstmt.setDate(2, task.getDate());
            pstmt.setString(3, task.getText());
            pstmt.setBoolean(4, task.isCompleted());
            pstmt.setInt(5, task.getFrequency());
            pstmt.setDate(6, task.getRepeatUntil());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a task from the database.
     * 
     * @param taskID The ID of the task
     * @param plantID The plant ID associated with the task
     * @return True if deletion successful, false otherwise
     */
    public boolean deleteTask(int taskID, int plantID) {
        // Delete from ASSIGNED_TO (foreign key constraint)
        String deleteAssignedSql = "DELETE FROM ASSIGNED_TO WHERE Task_ID = ? AND Plant_ID = ?";
        String deleteTaskSql = "DELETE FROM TASK WHERE Task_ID = ? AND Plant_ID = ?";
        
        try {
            // Delete assignments
            try (PreparedStatement pstmt = connection.prepareStatement(deleteAssignedSql)) {
                pstmt.setInt(1, taskID);
                pstmt.setInt(2, plantID);
                pstmt.executeUpdate();
            }
            
            // Delete the task
            try (PreparedStatement pstmt = connection.prepareStatement(deleteTaskSql)) {
                pstmt.setInt(1, taskID);
                pstmt.setInt(2, plantID);
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get a task by its composite key.
     * 
     * @param taskID The task ID
     * @param plantID The plant ID
     * @return Task object if found, null otherwise
     */
    public Task getTask(int taskID, int plantID) {
        String sql = "SELECT * FROM TASK WHERE Task_ID = ? AND Plant_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, taskID);
            pstmt.setInt(2, plantID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Task(
                    rs.getInt("Task_ID"),
                    rs.getInt("Plant_ID"),
                    rs.getDate("Date"),
                    rs.getString("Text"),
                    rs.getBoolean("isCompleted"),
                    rs.getInt("Frequency"),
                    rs.getDate("Repeat_Until")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all tasks for a specific plant.
     * 
     * @param plantID The plant ID
     * @return List of tasks for the plant
     */
    public List<Task> getTasksByPlant(int plantID) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM TASK WHERE Plant_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, plantID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Task task = new Task(
                    rs.getInt("Task_ID"),
                    rs.getInt("Plant_ID"),
                    rs.getDate("Date"),
                    rs.getString("Text"),
                    rs.getBoolean("isCompleted"),
                    rs.getInt("Frequency"),
                    rs.getDate("Repeat_Until")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
    
    /**
     * Get all tasks assigned to a specific user.
     * 
     * @param userID The user ID
     * @return List of tasks assigned to the user
     */
    public List<Task> getTasksByUser(int userID) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.Task_ID, t.Plant_ID, t.Date, t.Text, t.isCompleted, t.Frequency, t.Repeat_Until " +
                     "FROM TASK t " +
                     "JOIN ASSIGNED_TO a ON t.Task_ID = a.Task_ID AND t.Plant_ID = a.Plant_ID " +
                     "WHERE a.User_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Task task = new Task(
                    rs.getInt("Task_ID"),
                    rs.getInt("Plant_ID"),
                    rs.getDate("Date"),
                    rs.getString("Text"),
                    rs.getBoolean("isCompleted"),
                    rs.getInt("Frequency"),
                    rs.getDate("Repeat_Until")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
    
    /**
     * Get all users assigned to a specific task.
     * 
     * @param taskID The task ID
     * @param plantID The plant ID
     * @return List of user IDs assigned to the task
     */
    public List<Integer> getAssignedUsers(int taskID, int plantID) {
        List<Integer> userIDs = new ArrayList<>();
        String sql = "SELECT User_ID FROM ASSIGNED_TO WHERE Task_ID = ? AND Plant_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, taskID);
            pstmt.setInt(2, plantID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                userIDs.add(rs.getInt("User_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIDs;
    }
    
    /**
     * Unassign a task from a user.
     * 
     * @param taskID The task ID
     * @param plantID The plant ID
     * @param userID The user ID
     * @return True if unassignment successful, false otherwise
     */
    public boolean unassignTask(int taskID, int plantID, int userID) {
        String sql = "DELETE FROM ASSIGNED_TO WHERE Task_ID = ? AND Plant_ID = ? AND User_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, taskID);
            pstmt.setInt(2, plantID);
            pstmt.setInt(3, userID);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
