package com.gardenthyme.gui.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.gardenthyme.dao.GardenDAO;
import com.gardenthyme.database.DatabaseConnection;
import com.gardenthyme.gui.SceneManager;
import com.gardenthyme.gui.utils.GardenDataLoader;
import com.gardenthyme.models.Plant;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GardenController {
    @FXML
    private AnchorPane primaryPane;
    @FXML
    private HBox titleBar;
    @FXML
    private Button dropdownButton;
    @FXML
    private Button profileButton;
    @FXML
    private VBox baseVBox;
    @FXML
    private VBox contentVBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dropboxMenu;

    private int currentGardenID;
    private Label gardenTitle;
    private int currentUserID;
    private boolean isOwner;

    public void initialize(){
        GardenDataLoader.initializeConnection();
        
        currentGardenID = SceneManager.getTargetData();
        currentUserID = SceneManager.getCurrentUserID();
        if (currentGardenID == 0) {
            currentGardenID = 1;
        }

        SceneManager.setCurrentGardenID(currentGardenID);
        
        // Check if current user is owner
        isOwner = GardenDataLoader.isGardenOwner(currentGardenID, currentUserID);
        
        primaryPane.setStyle("-fx-background-color: #FFFFFF;");
        
        baseVBox.prefHeightProperty().bind(primaryPane.prefHeightProperty());
        baseVBox.prefWidthProperty().bind(primaryPane.prefWidthProperty());

        scrollPane.prefWidthProperty().bind(primaryPane.prefWidthProperty());
        scrollPane.prefHeightProperty().bind(primaryPane.prefHeightProperty().multiply(0.88));
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF; -fx-border-color: transparent;");
        
        StackPane holder = new StackPane(contentVBox);
        scrollPane.setContent(holder);
        contentVBox.prefWidthProperty().bind(primaryPane.prefWidthProperty().multiply(0.9));
        contentVBox.setAlignment(Pos.TOP_CENTER);
        contentVBox.setSpacing(15);
        contentVBox.setPadding(new Insets(15, 0, 15, 0));

        titleBar.prefHeightProperty().bind(primaryPane.prefHeightProperty().multiply(0.08));
        titleBar.setStyle("-fx-background-color: #F48FB1;");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 15, 0, 15));
        titleBar.setSpacing(10);
        
        gardenTitle = new Label();
        gardenTitle.setTextFill(Color.web("#4A0025"));
        gardenTitle.setWrapText(true);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        dropdownButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5; -fx-min-width: 30px; -fx-min-height: 30px;");
        profileButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;");

        VBox hamburgerMenu = new VBox();
        hamburgerMenu.setAlignment(Pos.CENTER);
        hamburgerMenu.setSpacing(4);
        hamburgerMenu.setPrefSize(24, 24);

        for (int i = 0; i < 3; i++) {
            Region line = new Region();
            line.setPrefSize(20, 2.5);
            line.setStyle("-fx-background-color: #4A0025; -fx-background-radius: 2;");
            hamburgerMenu.getChildren().add(line);
        }

        dropdownButton.setGraphic(hamburgerMenu);

        dropdownButton.setOnMouseEntered(e -> {
            for (int i = 0; i < 3; i++) {
                Region line = (Region) hamburgerMenu.getChildren().get(i);
                line.setStyle("-fx-background-color: #E91E63; -fx-background-radius: 2;");
            }
        });

        dropdownButton.setOnMouseExited(e -> {
            for (int i = 0; i < 3; i++) {
                Region line = (Region) hamburgerMenu.getChildren().get(i);
                line.setStyle("-fx-background-color: #4A0025; -fx-background-radius: 2;");
            }
        });

        ImageView profileIcon = new ImageView("/images/profileIcon.png");
        profileIcon.setFitWidth(20);
        profileIcon.setFitHeight(20);
        profileButton.setGraphic(profileIcon);

        titleBar.getChildren().clear();
        titleBar.getChildren().addAll(gardenTitle, spacer, dropdownButton, profileButton);

        dropboxMenu.setStyle("-fx-background-color: #F8BBD0; -fx-background-radius: 15;");
        dropboxMenu.prefWidthProperty().bind(primaryPane.prefWidthProperty());
        dropboxMenu.setAlignment(Pos.TOP_CENTER);
        dropboxMenu.setSpacing(10);
        dropboxMenu.setPadding(new Insets(15, 15, 15, 15));
        
        Button addBoxButton = new Button("➕ Add Garden Box");
        Button manageGardenersButton = new Button("👥 Manage Gardeners");
        Button plantipediaButton = new Button("🌿 Plantipedia");
        Button pestipediaButton = new Button("🐛 Pestipedia");
        
        styleDropdownButton(addBoxButton);
        styleDropdownButton(manageGardenersButton);
        styleDropdownButton(plantipediaButton);
        styleDropdownButton(pestipediaButton);
        
        addBoxButton.setOnAction(e -> showAddGardenBoxDialog());
        
        // Show Manage Gardeners button only if the current user is the garden owner
        if (isOwner) {
            manageGardenersButton.setVisible(true);
            manageGardenersButton.setManaged(true);
            manageGardenersButton.setOnAction(e -> showManageGardenersDialog());
        } else {
            manageGardenersButton.setVisible(false);
            manageGardenersButton.setManaged(false);
        }
        
        plantipediaButton.setOnAction(e -> {
            try {
                SceneManager.setTargetData(currentGardenID);
                SceneManager.setPage("plantipedia");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        pestipediaButton.setOnAction(e -> {
            try {
                SceneManager.setPage("pestipedia");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button retractDropdownButton = new Button("✕ Close");
        styleDropdownButton(retractDropdownButton);
        retractDropdownButton.setOnAction(e -> {
            dropboxMenu.setVisible(false);
            dropboxMenu.setManaged(false);
            dropdownButton.setVisible(true);
        });

        dropboxMenu.getChildren().addAll(addBoxButton, manageGardenersButton, plantipediaButton, pestipediaButton, retractDropdownButton);
        dropboxMenu.setVisible(false);
        dropboxMenu.setManaged(false);

        dropdownButton.setOnAction(e -> {
            dropboxMenu.setVisible(true);
            dropboxMenu.setManaged(true);
            dropdownButton.setVisible(false);
        });

        profileButton.setOnAction(e -> {
            try {
                SceneManager.setPage("profile");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        setupResponsiveTitleBar();
        loadGardenContents();
    }
    
    private void showManageGardenersDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Gardeners");
        dialog.setHeaderText("Manage people who have access to this garden");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");
        dialogPane.setPrefWidth(400);
        
        VBox mainContent = new VBox();
        mainContent.setSpacing(15);
        mainContent.setPadding(new Insets(10));
        
        // Current Gardeners
        Label currentLabel = new Label("👥 Current Garden Members");
        currentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        currentLabel.setTextFill(Color.web("#4A0025"));
        
        VBox gardenersList = new VBox();
        gardenersList.setSpacing(8);
        gardenersList.setPadding(new Insets(5, 0, 10, 0));
        
        // Load current gardeners
        loadCurrentGardeners(gardenersList);
        
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        
        // Add New Gardener
        Label addLabel = new Label("➕ Add New Gardener");
        addLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        addLabel.setTextFill(Color.web("#4A0025"));
        
        HBox searchBox = new HBox();
        searchBox.setSpacing(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Enter username or email");
        searchField.setStyle("-fx-background-radius: 10; -fx-padding: 8;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #F48FB1; -fx-text-fill: #4A0025; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 6 15 6 15;");
        
        Label searchResultLabel = new Label();
        searchResultLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        searchResultLabel.setWrapText(true);
        
        searchBox.getChildren().addAll(searchField, searchButton);
        
        mainContent.getChildren().addAll(currentLabel, gardenersList, separator, addLabel, searchBox, searchResultLabel);
        
        // Create a scrollable content
        ScrollPane scrollContent = new ScrollPane(mainContent);
        scrollContent.setFitToWidth(true);
        scrollContent.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF; -fx-border-color: transparent;");
        scrollContent.setPrefHeight(400);
        
        dialogPane.setContent(scrollContent);
        
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().add(closeButton);
        
        Button closeBtn = (Button) dialogPane.lookupButton(closeButton);
        closeBtn.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 6 15 6 15;");
        
        // Search functionality
        searchButton.setOnAction(e -> {
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                searchResultLabel.setText("Please enter a username or email.");
                searchResultLabel.setTextFill(Color.RED);
                return;
            }
            
            searchForGardener(searchText, searchResultLabel, gardenersList);
        });
        
        dialog.showAndWait();
    }
    
    private void loadCurrentGardeners(VBox gardenersList) {
        gardenersList.getChildren().clear();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT u.User_ID, u.Username, u.Email, m.IsOwner " +
                         "FROM USERS u " +
                         "JOIN MANAGES m ON u.User_ID = m.User_ID " +
                         "WHERE m.Garden_ID = ? " +
                         "ORDER BY m.IsOwner DESC, u.Username";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentGardenID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int userId = rs.getInt("User_ID");
                String username = rs.getString("Username");
                String email = rs.getString("Email");
                boolean isOwner = rs.getBoolean("IsOwner");
                
                HBox gardenerCard = createGardenerCard(userId, username, email, isOwner);
                gardenersList.getChildren().add(gardenerCard);
            }
            rs.close();
            pstmt.close();
            
            if (gardenersList.getChildren().isEmpty()) {
                Label emptyLabel = new Label("No gardeners found for this garden.");
                emptyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
                emptyLabel.setTextFill(Color.web("#9E9E9E"));
                gardenersList.getChildren().add(emptyLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading gardeners: " + e.getMessage());
            errorLabel.setTextFill(Color.RED);
            gardenersList.getChildren().add(errorLabel);
        }
    }
    
    private HBox createGardenerCard(int userId, String username, String email, boolean isOwner) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(10);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 10;");
        card.setEffect(new DropShadow(2, Color.rgb(216, 27, 96, 0.1)));
        
        VBox info = new VBox();
        info.setSpacing(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Label nameLabel = new Label(username);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nameLabel.setTextFill(Color.web("#4A0025"));
        
        String roleText = isOwner ? "👑 Owner" : "🤝 Manager";
        Label roleLabel = new Label(roleText);
        roleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        roleLabel.setTextFill(isOwner ? Color.web("#E91E63") : Color.web("#D81B60"));
        
        Label emailLabel = new Label(email);
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        emailLabel.setTextFill(Color.web("#9E9E9E"));
        
        info.getChildren().addAll(nameLabel, roleLabel, emailLabel);
        
        card.getChildren().add(info);
        
        // Only show remove button for non-owners (owners cannot be removed)
        if (!isOwner && this.isOwner) {
            Button removeBtn = new Button("Remove");
            removeBtn.setStyle(
                "-fx-background-color: #FFCDD2;" +
                "-fx-text-fill: #C62828;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 4 12 4 12;"
            );
            removeBtn.setOnAction(e -> {
                confirmRemoveGardener(userId, username);
            });
            card.getChildren().add(removeBtn);
        }
        
        return card;
    }
    
    private void searchForGardener(String searchText, Label resultLabel, VBox gardenersList) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Search for user by username or email
            String sql = "SELECT User_ID, Username, Email FROM USERS WHERE Username = ? OR Email = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, searchText);
            pstmt.setString(2, searchText);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("User_ID");
                String username = rs.getString("Username");
                String email = rs.getString("Email");
                
                // Check if user is already in this garden
                String checkSql = "SELECT * FROM MANAGES WHERE Garden_ID = ? AND User_ID = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setInt(1, currentGardenID);
                checkStmt.setInt(2, userId);
                ResultSet checkRs = checkStmt.executeQuery();
                
                if (checkRs.next()) {
                    resultLabel.setText(username + " is already a member of this garden.");
                    resultLabel.setTextFill(Color.ORANGE);
                    checkRs.close();
                    checkStmt.close();
                    rs.close();
                    pstmt.close();
                    return;
                }
                checkRs.close();
                checkStmt.close();
                
                // Check if user is yourself
                if (userId == currentUserID) {
                    resultLabel.setText("You are already the owner of this garden.");
                    resultLabel.setTextFill(Color.ORANGE);
                    rs.close();
                    pstmt.close();
                    return;
                }
                
                // User found, show add option
                resultLabel.setText("");
                
                HBox addCard = new HBox();
                addCard.setAlignment(Pos.CENTER_LEFT);
                addCard.setSpacing(10);
                addCard.setPadding(new Insets(8, 12, 8, 12));
                addCard.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 10;");
                
                VBox info = new VBox();
                info.setSpacing(3);
                HBox.setHgrow(info, Priority.ALWAYS);
                
                Label nameLabel = new Label(username);
                nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                nameLabel.setTextFill(Color.web("#2E7D32"));
                
                Label emailLabel = new Label(email);
                emailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
                emailLabel.setTextFill(Color.web("#9E9E9E"));
                
                info.getChildren().addAll(nameLabel, emailLabel);
                
                Button addBtn = new Button("Add to Garden");
                addBtn.setStyle(
                    "-fx-background-color: #4CAF50;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 12;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 4 12 4 12;"
                );
                addBtn.setOnAction(e -> {
                    addGardenerToGarden(userId, username, resultLabel, gardenersList);
                });
                
                addCard.getChildren().addAll(info, addBtn);
                
                // Replace the result label with the add card temporarily
                resultLabel.setGraphic(addCard);
                resultLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                
            } else {
                resultLabel.setText("No user found with username or email: " + searchText);
                resultLabel.setTextFill(Color.RED);
                resultLabel.setGraphic(null);
                resultLabel.setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            resultLabel.setText("Database error: " + e.getMessage());
            resultLabel.setTextFill(Color.RED);
            resultLabel.setGraphic(null);
            resultLabel.setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }
    
    private void addGardenerToGarden(int gardenerID, String username, Label resultLabel, VBox gardenersList) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if user exists and is a valid user
            String checkUserSql = "SELECT * FROM USERS WHERE User_ID = ?";
            PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql);
            checkUserStmt.setInt(1, gardenerID);
            ResultSet userRs = checkUserStmt.executeQuery();
            
            if (!userRs.next()) {
                resultLabel.setText("User does not exist.");
                resultLabel.setTextFill(Color.RED);
                resultLabel.setGraphic(null);
                resultLabel.setContentDisplay(ContentDisplay.TEXT_ONLY);
                userRs.close();
                checkUserStmt.close();
                return;
            }
            userRs.close();
            checkUserStmt.close();
            
            // Insert into MANAGES table as non-owner
            String sql = "INSERT INTO MANAGES (User_ID, Garden_ID, IsOwner) VALUES (?, ?, FALSE)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, gardenerID);
            pstmt.setInt(2, currentGardenID);
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            if (affectedRows > 0) {
                resultLabel.setText("✓ " + username + " has been added as a manager!");
                resultLabel.setTextFill(Color.GREEN);
                resultLabel.setGraphic(null);
                resultLabel.setContentDisplay(ContentDisplay.TEXT_ONLY);
                
                // Refresh the gardeners list
                loadCurrentGardeners(gardenersList);
                
                // Clear after 3 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        Platform.runLater(() -> {
                            resultLabel.setText("");
                        });
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } else {
                resultLabel.setText("Failed to add gardener. Please try again.");
                resultLabel.setTextFill(Color.RED);
                resultLabel.setGraphic(null);
                resultLabel.setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                resultLabel.setText(username + " is already managing this garden.");
                resultLabel.setTextFill(Color.ORANGE);
            } else {
                e.printStackTrace();
                resultLabel.setText("Database error: " + e.getMessage());
                resultLabel.setTextFill(Color.RED);
            }
            resultLabel.setGraphic(null);
            resultLabel.setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }
    
    private void confirmRemoveGardener(int gardenerID, String username) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Gardener");
        alert.setHeaderText("Remove " + username + " from this garden?");
        alert.setContentText("This gardener will no longer have access to this garden.");
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");
        
        Button confirmButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        confirmButton.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            removeGardenerFromGarden(gardenerID, username);
        }
    }
    
    private void removeGardenerFromGarden(int gardenerID, String username) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM MANAGES WHERE Garden_ID = ? AND User_ID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentGardenID);
            pstmt.setInt(2, gardenerID);
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            if (affectedRows > 0) {
                showAlert("Success", username + " has been removed from this garden.", Alert.AlertType.INFORMATION);
                // Refresh the garden view
                loadGardenContents();
            } else {
                showAlert("Error", "Failed to remove gardener.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void showAddGardenBoxDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Garden Box");
        dialog.setHeaderText("Add a new box to this garden");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");
        
        VBox form = new VBox();
        form.setSpacing(10);
        form.setPadding(new Insets(10));
        
        int nextBoxID = GardenDataLoader.getNextBoxID(currentGardenID);
        Label boxIDLabel = new Label("Box ID: " + nextBoxID + " (auto-assigned)");
        boxIDLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        boxIDLabel.setTextFill(Color.web("#4A0025"));
        
        TextField areaField = new TextField();
        areaField.setPromptText("Area (sq ft)");
        areaField.setStyle("-fx-background-radius: 10; -fx-padding: 8;");
        
        ComboBox<String> soilTypeCombo = new ComboBox<>();
        soilTypeCombo.getItems().addAll(GardenDataLoader.getSoilTypes());
        soilTypeCombo.setPromptText("Select Soil Type");
        soilTypeCombo.setStyle("-fx-background-radius: 10; -fx-padding: 8;");
        
        form.getChildren().addAll(
            new Label("Box Information:"),
            boxIDLabel,
            new Label("Area (square feet):"),
            areaField,
            new Label("Soil Type:"),
            soilTypeCombo
        );
        
        dialogPane.setContent(form);
        
        ButtonType createButton = new ButtonType("Add Box", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(createButton, cancelButton);
        
        Button createBtn = (Button) dialogPane.lookupButton(createButton);
        createBtn.setStyle("-fx-background-color: #F48FB1; -fx-text-fill: #4A0025; -fx-font-weight: bold;");
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == createButton) {
            String areaText = areaField.getText().trim();
            String soilType = soilTypeCombo.getValue();
            
            if (areaText.isEmpty()) {
                showAlert("Error", "Please enter an area for the box.", Alert.AlertType.ERROR);
                return;
            }
            
            if (soilType == null) {
                showAlert("Error", "Please select a soil type.", Alert.AlertType.ERROR);
                return;
            }
            
            try {
                float area = Float.parseFloat(areaText);
                if (area <= 0) {
                    showAlert("Error", "Area must be greater than 0.", Alert.AlertType.ERROR);
                    return;
                }
                
                boolean success = GardenDataLoader.addGardenBox(currentGardenID, nextBoxID, area, soilType);
                if (success) {
                    showAlert("Success", "Garden box added successfully!", Alert.AlertType.INFORMATION);
                    loadGardenContents();
                } else {
                    showAlert("Error", "Failed to add garden box. Please try again.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid number for area.", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");
        
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #F48FB1; -fx-text-fill: #4A0025; -fx-font-weight: bold;");
        
        alert.showAndWait();
    }
    
    private void setupResponsiveTitleBar() {
        primaryPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            String fullGardenName = getGardenName(currentGardenID);
            
            if (width < 350) {
                String icon = getGardenIcon(currentGardenID);
                gardenTitle.setText(icon);
                gardenTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
                gardenTitle.setMaxWidth(40);
                
            } else if (width < 400) {
                String shortName = getShortGardenName(currentGardenID);
                gardenTitle.setText(shortName);
                gardenTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
                gardenTitle.setMaxWidth(100);
                
            } else if (width < 480) {
                gardenTitle.setText(fullGardenName);
                gardenTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                gardenTitle.setMaxWidth(180);
                
            } else {
                gardenTitle.setText(fullGardenName);
                gardenTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                gardenTitle.setMaxWidth(250);
            }
        });
        
        primaryPane.requestLayout();
    }
    
    private String getGardenIcon(int gardenID) {
        switch(gardenID) {
            case 1: return "🌱";
            case 2: return "🌿";
            case 3: return "🌸";
            case 4: return "🌻";
            default: return "🌱";
        }
    }
    
    private String getShortGardenName(int gardenID) {
        switch(gardenID) {
            case 1: return "🌱 Veg Garden";
            case 2: return "🌿 Herb Garden";
            case 3: return "🌸 Flower Garden";
            case 4: return "🌻 Community Garden";
            default: return "🌱 My Garden";
        }
    }
    
    private String getGardenName(int gardenID) {
        switch(gardenID) {
            case 1: return "🌱 My Vegetable Garden";
            case 2: return "🌿 Herb Garden";
            case 3: return "🌸 Flower Paradise";
            case 4: return "🌻 Community Garden";
            default: return "🌱 My Garden";
        }
    }
    
    private void styleDropdownButton(Button button) {
        button.setStyle(
            "-fx-background-color: #F48FB1;" +
            "-fx-text-fill: #4A0025;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 15 8 15;"
        );
        button.setMaxWidth(200);
        
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: #E91E63;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 15 8 15;"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: #F48FB1;" +
                "-fx-text-fill: #4A0025;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 15 8 15;"
            );
        });
    }

    private void loadGardenContents() {
        contentVBox.getChildren().clear();
        
        List<Integer> boxes = GardenDataLoader.getGardenBoxes(currentGardenID);
        Map<Integer, List<Plant>> boxPlants = GardenDataLoader.getPlantsInGardenBoxes(currentGardenID);
        
        System.out.println("Loading garden " + currentGardenID + " with " + boxes.size() + " boxes");
        
        if (boxes.isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50, 20, 50, 20));
            emptyBox.setSpacing(10);
            
            Label emptyLabel = new Label("🌱 No garden boxes yet!");
            emptyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            emptyLabel.setTextFill(Color.web("#4A0025"));
            
            Label instructionLabel = new Label("Click the ☰ button and select 'Add Garden Box'");
            instructionLabel.setFont(Font.font("Segoe UI", 12));
            instructionLabel.setTextFill(Color.web("#D81B60"));
            
            emptyBox.getChildren().addAll(emptyLabel, instructionLabel);
            contentVBox.getChildren().add(emptyBox);
            return;
        }
        
        for (int boxID : boxes) {
            VBox gardenBox = generateGardenBox(boxID);
            List<Plant> plants = boxPlants.getOrDefault(boxID, new java.util.ArrayList<>());

            if (plants.isEmpty()) {
                Label emptyBoxLabel = new Label("  🌿 This box is empty. Add plants from Plantipedia!");
                emptyBoxLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
                emptyBoxLabel.setTextFill(Color.web("#D81B60"));
                emptyBoxLabel.setPadding(new Insets(10, 0, 10, 15));
                gardenBox.getChildren().add(emptyBoxLabel);
            } else {
                for (Plant plant : plants) {
                    gardenBox.getChildren().add(generatePlantCard(plant, boxID));
                }
            }
            
            contentVBox.getChildren().add(gardenBox);
        }
    }

    private VBox generateGardenBox(int boxID){
        VBox box = new VBox();
        box.setId("box_" + boxID);
        box.prefWidthProperty().bind(contentVBox.prefWidthProperty());
        box.setAlignment(Pos.TOP_LEFT);
        box.setSpacing(8);
        box.setPadding(new Insets(12, 12, 12, 12));
        box.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 15;");
        box.setEffect(new DropShadow(3, Color.rgb(216, 27, 96, 0.15)));
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        header.setPadding(new Insets(0, 0, 8, 0));
        
        Label boxLabel = new Label("📦 Box " + boxID);
        boxLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        boxLabel.setTextFill(Color.web("#4A0025"));

        Label boxSizeLabel = new Label();
        boxSizeLabel.setText(String.valueOf(GardenDataLoader.getGardenBoxArea(boxID, currentGardenID)) + " sq ft.  ");

        Label boxpHLabel = new Label();
        boxpHLabel.setText("pH: " + String.valueOf(GardenDataLoader.getGardenBoxPH(boxID, currentGardenID)));

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Button addPlantBtn = new Button("+ Add Plant");
        addPlantBtn.setStyle(
            "-fx-background-color: #F48FB1;" +
            "-fx-text-fill: #4A0025;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 4 10 4 10;"
        );
        addPlantBtn.setOnAction(e -> {
            try {
                SceneManager.setTargetData(boxID);
                SceneManager.setPage("plantipedia");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        
        header.getChildren().addAll(boxLabel, boxSizeLabel, boxpHLabel, headerSpacer, addPlantBtn);
        box.getChildren().add(header);
        
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #F48FB1;");
        separator.prefWidthProperty().bind(box.prefWidthProperty());
        box.getChildren().add(separator);
        
        return box;
    }

    private HBox generatePlantCard(Plant plant, int boxID){
        HBox box = new HBox();
        box.prefWidthProperty().bind(contentVBox.prefWidthProperty().subtract(30));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(8);
        box.setPadding(new Insets(8, 12, 8, 12));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;");
        box.setEffect(new DropShadow(2, Color.rgb(0, 0, 0, 0.08)));

        VBox names = new VBox();
        names.setSpacing(2);
        names.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(names, Priority.ALWAYS);

        Label commonName = new Label(plant.getCommonName());
        commonName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        commonName.setTextFill(Color.web("#4A0025"));

        Label scienceName = new Label(plant.getScientificName());
        scienceName.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        scienceName.setTextFill(Color.web("#D81B60"));

        names.getChildren().addAll(commonName, scienceName);

        // Task indicator
        HBox taskContainer = new HBox();
        taskContainer.setAlignment(Pos.CENTER_RIGHT);
        taskContainer.setSpacing(4);
        taskContainer.setMinWidth(60);
        
        Label taskIcon = new Label(plant.getPendingTasks() > 0 ? "📋" : "✅");
        taskIcon.setFont(Font.font(12));
        
        Label taskAmount = new Label(plant.getPendingTasks() + " tasks");
        taskAmount.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        taskAmount.setTextFill(plant.getPendingTasks() > 0 ? Color.web("#E91E63") : Color.web("#4CAF50"));
        
        taskContainer.getChildren().addAll(taskIcon, taskAmount);
        
        // Remove button
        Button removeBtn = new Button("✕");
        removeBtn.setStyle(
            "-fx-background-color: #FFCDD2;" +
            "-fx-text-fill: #C62828;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 4 8 4 8;"
        );
        removeBtn.setMinWidth(28);
        Tooltip removeTooltip = new Tooltip("Remove this plant");
        Tooltip.install(removeBtn, removeTooltip);
        removeBtn.setOnAction(e -> {
            showRemoveConfirmation(plant, boxID);
        });

        box.getChildren().addAll(names, taskContainer, removeBtn);
        
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 15;");
        });
        
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;");
        });

        box.setOnMouseClicked(e -> {
            try {
                SceneManager.setTargetData(plant.getPlantID());
                SceneManager.setPage("myPlantInfo");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        
        return box;
    }
    
    private void showRemoveConfirmation(Plant plant, int boxID) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Plant");
        alert.setHeaderText("Remove " + plant.getCommonName() + "?");
        alert.setContentText("This will remove this plant from the garden box.");
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");
        
        Button confirmButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        confirmButton.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = GardenDataLoader.removePlantFromGardenBox(plant.getPlantID(), currentGardenID, boxID);
            if (success) {
                showAlert("Success", plant.getCommonName() + " removed from the garden.", Alert.AlertType.INFORMATION);
                loadGardenContents();
            } else {
                showAlert("Error", "Failed to remove plant.", Alert.AlertType.ERROR);
            }
        }
    }
}
