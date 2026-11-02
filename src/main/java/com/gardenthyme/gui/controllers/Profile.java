package com.gardenthyme.gui.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.gardenthyme.database.DatabaseConnection;
import com.gardenthyme.gui.SceneManager;
import com.gardenthyme.gui.utils.GardenDataLoader;
import com.gardenthyme.models.Garden;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class Profile {
    @FXML
    private AnchorPane primaryPane;
    @FXML
    private HBox titleBar;
    @FXML
    private VBox baseVBox;
    @FXML
    private VBox contentVBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button logoutButton;
    @FXML
    private Button adminModeButton;

    private int currentUserID;
    private Button createGardenButton;
    private Label titleLabel;

    public void initialize(){
        GardenDataLoader.initializeConnection();
        
        currentUserID = SceneManager.getCurrentUserID();
        System.out.println("Profile initialized with User ID: " + currentUserID);
        
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
        contentVBox.setSpacing(12);
        contentVBox.setPadding(new Insets(15, 0, 15, 0));

        titleBar.prefHeightProperty().bind(primaryPane.prefHeightProperty().multiply(0.08));
        titleBar.setStyle("-fx-background-color: #F48FB1;");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 15, 0, 15));

        titleLabel = new Label("👤 My Gardens");
        titleLabel.setTextFill(Color.web("#4A0025"));
        titleLabel.setWrapText(true);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox buttonContainer = new HBox(8);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        
        // Check if user is admin using database query
        boolean isAdmin = checkIfAdmin(currentUserID);
        System.out.println("User " + currentUserID + " is admin: " + isAdmin);
        
        adminModeButton = new Button();
        adminModeButton.setStyle(
            "-fx-background-color: #E91E63;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;"
        );
        
        addHoverAnimation(adminModeButton);
        
        if(isAdmin) {   
            adminModeButton.setText("Admin Mode");
            adminModeButton.setVisible(true);
            adminModeButton.setManaged(true);
            adminModeButton.setOnAction(actionEvent -> {
                try {
                    SceneManager.setPage("adminview");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            buttonContainer.getChildren().add(adminModeButton);
        } else {
            adminModeButton.setVisible(false);
            adminModeButton.setManaged(false);
        }
        
        createGardenButton = new Button();
        createGardenButton.setStyle(
            "-fx-background-color: #E91E63;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;"
        );
        addHoverAnimation(createGardenButton);
        createGardenButton.setOnAction(e -> showCreateGardenDialog());
        buttonContainer.getChildren().add(createGardenButton);
        
        logoutButton = new Button();
        logoutButton.setStyle(
            "-fx-background-color: #D81B60;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;"
        );
        addHoverAnimation(logoutButton);
        
        logoutButton.setOnAction(e -> {
            try {
                SceneManager.setCurrentUserID(0);
                SceneManager.setPage("startup");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonContainer.getChildren().add(logoutButton);
        
        titleBar.getChildren().clear();
        titleBar.getChildren().addAll(titleLabel, spacer, buttonContainer);
        
        setupResponsiveTitleBar();
        loadUserGardens();
        
        primaryPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            loadUserGardens();
        });
    }
    
    /**
     * Check if a user is an admin by querying the ADMIN table
     */
    private boolean checkIfAdmin(int userID) {
        if (userID <= 0) return false;
        
        String sql = "SELECT * FROM ADMIN WHERE User_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking admin status for user " + userID);
            e.printStackTrace();
            return false;
        }
    }
    
    private void addHoverAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }
    
    private void setupResponsiveTitleBar() {
        primaryPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            
            if (width < 380) {
                titleLabel.setText("🌱");
                titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
                titleLabel.setMaxWidth(40);
                
                if (adminModeButton.isVisible()) {
                    adminModeButton.setText("👑");
                    adminModeButton.setMinWidth(35);
                    adminModeButton.setMaxWidth(35);
                }
                createGardenButton.setText("+");
                createGardenButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                createGardenButton.setMinWidth(35);
                createGardenButton.setMaxWidth(35);
                logoutButton.setText("🚪");
                logoutButton.setMinWidth(35);
                logoutButton.setMaxWidth(35);
                
            } else if (width < 480) {
                titleLabel.setText("👤 My Gardens");
                titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                titleLabel.setMaxWidth(150);
                
                if (adminModeButton.isVisible()) {
                    adminModeButton.setText("Admin");
                    adminModeButton.setMinWidth(55);
                }
                createGardenButton.setText("+ Garden");
                createGardenButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                createGardenButton.setMinWidth(65);
                logoutButton.setText("Logout");
                logoutButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                logoutButton.setMinWidth(60);
                
            } else {
                titleLabel.setText("👤 My Gardens");
                titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                titleLabel.setMaxWidth(150);
                
                if (adminModeButton.isVisible()) {
                    adminModeButton.setText("Admin Mode");
                    adminModeButton.setMinWidth(85);
                }
                createGardenButton.setText("+ New Garden");
                createGardenButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                createGardenButton.setMinWidth(95);
                logoutButton.setText("Logout");
                logoutButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                logoutButton.setMinWidth(65);
            }
        });
        
        primaryPane.requestLayout();
    }
    
    private void showCreateGardenDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Garden");
        dialog.setHeaderText("Create a new garden");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");
        
        VBox form = new VBox();
        form.setSpacing(10);
        form.setPadding(new Insets(10));
        
        TextField gardenNameField = new TextField();
        gardenNameField.setPromptText("Garden Name");
        gardenNameField.setStyle("-fx-background-radius: 10; -fx-padding: 8;");
        
        Label infoLabel = new Label("You will be the owner of this garden.");
        infoLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        infoLabel.setTextFill(Color.web("#D81B60"));
        
        form.getChildren().addAll(
            new Label("Garden Name:"),
            gardenNameField,
            infoLabel
        );
        
        dialogPane.setContent(form);
        
        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(createButton, cancelButton);
        
        Button createBtn = (Button) dialogPane.lookupButton(createButton);
        createBtn.setStyle("-fx-background-color: #F48FB1; -fx-text-fill: #4A0025; -fx-font-weight: bold;");
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == createButton) {
            String gardenName = gardenNameField.getText().trim();
            if (!gardenName.isEmpty()) {
                if (GardenDataLoader.createGarden(gardenName, currentUserID)) {
                    loadUserGardens();
                    showAlert("Success", "Garden '" + gardenName + "' created successfully!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to create garden. Please try again.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Please enter a garden name.", Alert.AlertType.WARNING);
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
    
    private void loadUserGardens() {
        contentVBox.getChildren().clear();
        
        List<Garden> gardens = GardenDataLoader.getUserGardens(currentUserID);
        
        double currentWidth = primaryPane.getWidth();
        if (currentWidth <= 0) currentWidth = 400;
        
        if (gardens.isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50, 20, 50, 20));
            emptyBox.setSpacing(15);
            
            Label emptyLabel = new Label("🌱 No gardens yet!");
            emptyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            emptyLabel.setTextFill(Color.web("#4A0025"));
            
            Label instructionLabel = new Label("Click the '+ New Garden' button to create one");
            instructionLabel.setFont(Font.font("Segoe UI", 12));
            instructionLabel.setTextFill(Color.web("#D81B60"));
            
            emptyBox.getChildren().addAll(emptyLabel, instructionLabel);
            contentVBox.getChildren().add(emptyBox);
            return;
        }
        
        for (Garden garden : gardens) {
            contentVBox.getChildren().add(generateCard(garden, currentWidth));
        }
    }

    private HBox generateCard(Garden garden, double parentWidth){
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(10);
        box.setPadding(new Insets(10, 15, 10, 15));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20;");
        box.setEffect(new DropShadow(3, Color.rgb(216, 27, 96, 0.1)));
        
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        
        boolean isNarrow = parentWidth < 400;
        
        String icon = garden.getGardenName().contains("Vegetable") ? "🥕" :
                      garden.getGardenName().contains("Herb") ? "🌿" :
                      garden.getGardenName().contains("Flower") ? "🌸" : "🌻";
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(isNarrow ? 24 : 28));
        iconLabel.setMinWidth(isNarrow ? 40 : 50);
        iconLabel.setMaxWidth(isNarrow ? 40 : 50);
        
        VBox names = new VBox();
        names.setSpacing(4);
        names.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(names, Priority.ALWAYS);
        
        Label gardenName = new Label(garden.getGardenName());
        gardenName.setFont(Font.font("Segoe UI", FontWeight.BOLD, isNarrow ? 14 : 16));
        gardenName.setTextFill(Color.web("#4A0025"));
        gardenName.setWrapText(true);
        gardenName.setMaxWidth(parentWidth * 0.4);

        String ownerText = garden.isOwner() ? "👑 Owner" : "🤝 Manager";
        Label ownerLabel = new Label(ownerText);
        ownerLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, isNarrow ? 10 : 11));
        ownerLabel.setTextFill(Color.web("#D81B60"));

        names.getChildren().addAll(gardenName, ownerLabel);

        int plantCount = GardenDataLoader.getPlantCountInGarden(garden.getGardenID());
        Label plantCountLabel = new Label("🌱 " + plantCount);
        plantCountLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, isNarrow ? 12 : 13));
        plantCountLabel.setTextFill(Color.web("#D81B60"));
        plantCountLabel.setMinWidth(45);
        plantCountLabel.setMaxWidth(45);
        plantCountLabel.setAlignment(Pos.CENTER);
        
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setMinWidth(50);
        
        if (garden.isOwner()) {
            Button deleteButton = new Button("✕");
            deleteButton.setStyle(
                "-fx-background-color: #FFCDD2;" +
                "-fx-text-fill: #C62828;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 15;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 6 12 6 12;" +
                "-fx-min-width: 40px;" +
                "-fx-min-height: 32px;"
            );
            addHoverAnimation(deleteButton);
            Tooltip deleteTooltip = new Tooltip("Delete Garden");
            Tooltip.install(deleteButton, deleteTooltip);
            deleteButton.setOnAction(e -> {
                e.consume();
                showDeleteConfirmation(garden);
            });
            actionButtons.getChildren().add(deleteButton);
        } else {
            Button leaveButton = new Button("🚪");
            leaveButton.setStyle(
                "-fx-background-color: #FFE0B2;" +
                "-fx-text-fill: #E65100;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 15;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 6 12 6 12;" +
                "-fx-min-width: 40px;" +
                "-fx-min-height: 32px;"
            );
            addHoverAnimation(leaveButton);
            Tooltip leaveTooltip = new Tooltip("Leave Garden");
            Tooltip.install(leaveButton, leaveTooltip);
            leaveButton.setOnAction(e -> {
                e.consume();
                showLeaveConfirmation(garden);
            });
            actionButtons.getChildren().add(leaveButton);
        }
        
        Label arrowLabel = new Label("→");
        arrowLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, isNarrow ? 14 : 16));
        arrowLabel.setTextFill(Color.web("#F48FB1"));
        arrowLabel.setMinWidth(25);
        arrowLabel.setMaxWidth(25);
        arrowLabel.setAlignment(Pos.CENTER);

        box.getChildren().addAll(iconLabel, names, plantCountLabel, actionButtons, arrowLabel);
        
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 20;");
            ScaleTransition st = new ScaleTransition(Duration.millis(150), box);
            st.setToX(1.01);
            st.setToY(1.01);
            st.play();
        });
        
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20;");
            ScaleTransition st = new ScaleTransition(Duration.millis(150), box);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        box.setOnMouseClicked(e -> {
            try {
                SceneManager.setTargetData(garden.getGardenID());
                SceneManager.setPage("garden");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        
        return box;
    }
    
    private void showDeleteConfirmation(Garden garden) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Garden");
        alert.setHeaderText("Delete " + garden.getGardenName() + "?");
        alert.setContentText("Everything in this garden will be permanently removed.");

        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");
        
        Button confirmButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        confirmButton.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-weight: bold;");
        addHoverAnimation(confirmButton);
        
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold;");
        addHoverAnimation(cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = GardenDataLoader.deleteGarden(garden.getGardenID(), currentUserID);
            if (success) {
                showAlert("Success", "Garden deleted successfully.", Alert.AlertType.INFORMATION);
                loadUserGardens();
            } else {
                showAlert("Error", "Failed to delete garden.", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void showLeaveConfirmation(Garden garden) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Garden");
        alert.setHeaderText("Leave " + garden.getGardenName() + "?");
        alert.setContentText("You will no longer have access to this garden as a manager.");
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");
        
        Button confirmButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        confirmButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        addHoverAnimation(confirmButton);
        
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold;");
        addHoverAnimation(cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = GardenDataLoader.leaveGarden(garden.getGardenID(), currentUserID);
            if (success) {
                showAlert("Success", "You have left the garden.", Alert.AlertType.INFORMATION);
                loadUserGardens();
            } else {
                showAlert("Error", "Failed to leave garden.", Alert.AlertType.ERROR);
            }
        }
    }
}
