package com.gardenthyme.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gardenthyme.models.Garden;

/**
 * GardenDAO handles all database operations for the GARDEN table.
 * 
 * @author Garden Thyme Group (G-17)
 * @version 1.0
 */
public class GardenDAO {
    
    private Connection connection;
    
    public GardenDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Add a new garden to the system.
     * 
     * @param gardenID The ID of the garden to add
     * @param gardenName The name of the garden
     * @return True if addition successful, false otherwise
     */
    public boolean addGarden(int gardenID, String gardenName) {
        String checkSql = "SELECT * FROM GARDEN WHERE Garden_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Garden " + gardenID + " already exists.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        String sql = "INSERT INTO GARDEN (Garden_ID, Garden_Name) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setString(2, gardenName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Rename an existing garden (changes name only, not ID).
     * Only the garden owner can do this.
     * 
     * @param gardenID The ID of the garden to rename
     * @param newGardenName The new name for the garden
     * @param requestingUserID The user requesting the rename (must be owner)
     * @return True if rename successful, false otherwise
     */
    public boolean renameGarden(int gardenID, String newGardenName, int requestingUserID) {
        // Check if requester is the owner
        if (!isOwner(gardenID, requestingUserID)) {
            System.out.println("User " + requestingUserID + " is not the owner of garden " + gardenID);
            return false;
        }
        
        // Check if garden exists
        if (getGardenByID(gardenID) == null) {
            System.out.println("Garden " + gardenID + " does not exist.");
            return false;
        }
        
        // Update the garden name
        String sql = "UPDATE GARDEN SET Garden_Name = ? WHERE Garden_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newGardenName);
            pstmt.setInt(2, gardenID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a garden from the system.
     * Only the garden owner can do this.
     * 
     * @param gardenID The ID of the garden to delete
     * @param requestingUserID The user requesting deletion (must be owner)
     * @return True if deletion successful, false otherwise
     */
    public boolean deleteGarden(int gardenID, int requestingUserID) {
        // Check if requester is the owner
        if (!isOwner(gardenID, requestingUserID)) {
            System.out.println("User " + requestingUserID + " is not the owner of garden " + gardenID);
            return false;
        }
        
        // Check if garden exists
        if (getGardenByID(gardenID) == null) {
            System.out.println("Garden " + gardenID + " does not exist.");
            return false;
        }
        
        String sql = "DELETE FROM GARDEN WHERE Garden_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== Ownership and Management Methods =====
    
    /**
     * Assign a user as the owner of a garden.
     * Should be called when creating a new garden.
     * 
     * @param gardenID The garden ID
     * @param userID The user ID to assign as owner
     * @return True if assignment successful, false otherwise
     */
    public boolean assignOwner(int gardenID, int userID) {
        // Check if garden already has an owner
        String checkOwnerSql = "SELECT * FROM MANAGES WHERE Garden_ID = ? AND IsOwner = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(checkOwnerSql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Garden " + gardenID + " already has an owner.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Check if user exists
        String checkUserSql = "SELECT * FROM USERS WHERE User_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkUserSql)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("User " + userID + " does not exist.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Check if user is already in MANAGES for this garden
        String checkSql = "SELECT * FROM MANAGES WHERE User_ID = ? AND Garden_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, gardenID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String updateSql = "UPDATE MANAGES SET IsOwner = TRUE WHERE User_ID = ? AND Garden_ID = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, userID);
                    updateStmt.setInt(2, gardenID);
                    return updateStmt.executeUpdate() > 0;
                }
            } else {
                String insertSql = "INSERT INTO MANAGES (User_ID, Garden_ID, IsOwner) VALUES (?, ?, TRUE)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userID);
                    insertStmt.setInt(2, gardenID);
                    return insertStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add a manager (non-owner) to a garden.
     * Only the garden owner can do this.
     * 
     * @param gardenID The garden ID
     * @param currentUserID The user adding the manager (must be owner)
     * @param newManagerID The user ID to add as manager
     * @return True if addition successful, false otherwise
     */
    public boolean addManager(int gardenID, int currentUserID, int newManagerID) {
        // Check if current user is the owner
        if (!isOwner(gardenID, currentUserID)) {
            System.out.println("User " + currentUserID + " is not the owner of garden " + gardenID);
            return false;
        }
        
        // Cannot add yourself as manager
        if (currentUserID == newManagerID) {
            System.out.println("You are already the owner of this garden.");
            return false;
        }
        
        // Check if new manager exists
        String checkUserSql = "SELECT * FROM USERS WHERE User_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkUserSql)) {
            pstmt.setInt(1, newManagerID);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("User " + newManagerID + " does not exist.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Check if already in MANAGES
        String checkSql = "SELECT * FROM MANAGES WHERE User_ID = ? AND Garden_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setInt(1, newManagerID);
            pstmt.setInt(2, gardenID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("User " + newManagerID + " already manages garden " + gardenID);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Add as manager (non-owner)
        String sql = "INSERT INTO MANAGES (User_ID, Garden_ID, IsOwner) VALUES (?, ?, FALSE)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newManagerID);
            pstmt.setInt(2, gardenID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Remove a user from a garden.
     * - Owner can remove any manager
     * - A regular manager can remove themselves (leave garden)
     * - Owner cannot be removed (must transfer ownership or delete garden)
     * 
     * @param gardenID The garden ID
     * @param currentUserID The user requesting the removal
     * @param userToRemoveID The user ID to remove
     * @return True if removal successful, false otherwise
     */
    public boolean removeFromGarden(int gardenID, int currentUserID, int userToRemoveID) {
        // Check if the user to remove exists in MANAGES
        String checkSql = "SELECT IsOwner FROM MANAGES WHERE User_ID = ? AND Garden_ID = ?";
        boolean isTargetOwner = false;
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setInt(1, userToRemoveID);
            pstmt.setInt(2, gardenID);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("User " + userToRemoveID + " does not manage garden " + gardenID);
                return false;
            }
            isTargetOwner = rs.getBoolean("IsOwner");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Cannot remove the owner
        if (isTargetOwner) {
            System.out.println("Cannot remove the garden owner. Transfer ownership or delete the garden instead.");
            return false;
        }
        
        // Case 1: User is removing themselves (leaving the garden)
        if (currentUserID == userToRemoveID) {
            String sql = "DELETE FROM MANAGES WHERE User_ID = ? AND Garden_ID = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, userToRemoveID);
                pstmt.setInt(2, gardenID);
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        // Case 2: User is trying to remove someone else (must be owner)
        if (!isOwner(gardenID, currentUserID)) {
            System.out.println("User " + currentUserID + " is not the owner of garden " + gardenID);
            return false;
        }
        
        // Owner removing a manager
        String sql = "DELETE FROM MANAGES WHERE User_ID = ? AND Garden_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userToRemoveID);
            pstmt.setInt(2, gardenID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Transfer ownership of a garden to another user.
     * 
     * @param gardenID The garden ID
     * @param currentOwnerID The current owner
     * @param newOwnerID The user to transfer ownership to
     * @return True if transfer successful, false otherwise
     */
    public boolean transferOwnership(int gardenID, int currentOwnerID, int newOwnerID) {
        // Check if current user is the owner
        if (!isOwner(gardenID, currentOwnerID)) {
            System.out.println("User " + currentOwnerID + " is not the owner of garden " + gardenID);
            return false;
        }
        
        // Check if new owner exists and is a manager of this garden
        String checkSql = "SELECT * FROM MANAGES WHERE User_ID = ? AND Garden_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setInt(1, newOwnerID);
            pstmt.setInt(2, gardenID);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("User " + newOwnerID + " does not manage garden " + gardenID);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Transfer ownership
        String sql = "UPDATE MANAGES SET IsOwner = CASE " +
                     "WHEN User_ID = ? THEN FALSE " +
                     "WHEN User_ID = ? THEN TRUE " +
                     "END WHERE Garden_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, currentOwnerID);
            pstmt.setInt(2, newOwnerID);
            pstmt.setInt(3, gardenID);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a user is the owner of a garden.
     * 
     * @param gardenID The garden ID
     * @param userID The user ID to check
     * @return True if user is the owner, false otherwise
     */
    public boolean isOwner(int gardenID, int userID) {
        String sql = "SELECT * FROM MANAGES WHERE Garden_ID = ? AND User_ID = ? AND IsOwner = TRUE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, userID);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a user can manage a garden (either as owner or manager).
     * 
     * @param gardenID The garden ID
     * @param userID The user ID to check
     * @return True if user manages the garden, false otherwise
     */
    public boolean canManageGarden(int gardenID, int userID) {
        String sql = "SELECT * FROM MANAGES WHERE Garden_ID = ? AND User_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            pstmt.setInt(2, userID);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all garden IDs that a user has access to (both owned and managed).
     * 
     * @param userID The user ID
     * @return List of garden IDs the user can access
     */
    public List<Integer> getGardenIDsByUser(int userID) {
        List<Integer> gardenIDs = new ArrayList<>();
        String sql = "SELECT Garden_ID FROM MANAGES WHERE User_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                gardenIDs.add(rs.getInt("Garden_ID"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting gardens for user " + userID);
            e.printStackTrace();
        }
        return gardenIDs;
    }
    
    /**
     * Get all gardens a user owns.
     * 
     * @param userID The user ID
     * @return List of garden IDs the user owns
     */
    public List<Integer> getOwnedGardenIDs(int userID) {
        List<Integer> gardenIDs = new ArrayList<>();
        String sql = "SELECT Garden_ID FROM MANAGES WHERE User_ID = ? AND IsOwner = TRUE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                gardenIDs.add(rs.getInt("Garden_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gardenIDs;
    }
    
    /**
     * Get all gardens a user manages (but does not own).
     * 
     * @param userID The user ID
     * @return List of garden IDs the user manages (non-owner)
     */
    public List<Integer> getManagedGardenIDs(int userID) {
        List<Integer> gardenIDs = new ArrayList<>();
        String sql = "SELECT Garden_ID FROM MANAGES WHERE User_ID = ? AND IsOwner = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                gardenIDs.add(rs.getInt("Garden_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gardenIDs;
    }
    
    /**
     * Check if a user owns a specific garden.
     * 
     * @param userID The user ID
     * @param gardenID The garden ID to check
     * @return True if user owns this garden, false otherwise
     */
    public boolean doesUserOwnGarden(int userID, int gardenID) {
        return isOwner(gardenID, userID);
    }
    
    // ===== Helper Methods =====
    
    /**
     * Get a garden by its ID.
     * 
     * @param gardenID The garden ID
     * @return Garden object if found, null otherwise
     */
    public Garden getGardenByID(int gardenID) {
        String sql = "SELECT * FROM GARDEN WHERE Garden_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Garden(
                    rs.getInt("Garden_ID"),
                    rs.getString("Garden_Name")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get a garden's name by its ID.
     * 
     * @param gardenID The garden ID
     * @return Garden name if found, null otherwise
     */
    public String getGardenName(int gardenID) {
        String sql = "SELECT Garden_Name FROM GARDEN WHERE Garden_ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("Garden_Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all gardens in the system.
     * 
     * @return List of all Garden objects
     */
    public List<Garden> getAllGardens() {
        List<Garden> gardens = new ArrayList<>();
        String sql = "SELECT * FROM GARDEN";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Garden garden = new Garden(
                    rs.getInt("Garden_ID"),
                    rs.getString("Garden_Name")
                );
                gardens.add(garden);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gardens;
    }
    
    /**
     * Get all managers (non-owners) of a garden.
     * 
     * @param gardenID The garden ID
     * @return List of user IDs who manage but don't own the garden
     */
    public List<Integer> getManagers(int gardenID) {
        List<Integer> managers = new ArrayList<>();
        String sql = "SELECT User_ID FROM MANAGES WHERE Garden_ID = ? AND IsOwner = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                managers.add(rs.getInt("User_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return managers;
    }
    
    /**
     * Get the owner of a garden.
     * 
     * @param gardenID The garden ID
     * @return User ID of the owner, or -1 if not found
     */
    public int getOwner(int gardenID) {
        String sql = "SELECT User_ID FROM MANAGES WHERE Garden_ID = ? AND IsOwner = TRUE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gardenID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("User_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
