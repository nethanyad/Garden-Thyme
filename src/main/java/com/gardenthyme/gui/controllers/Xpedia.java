package com.gardenthyme.gui.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.gardenthyme.dao.PestDAO;
import com.gardenthyme.dao.PlantDAO;
import com.gardenthyme.database.DatabaseConnection;
import com.gardenthyme.gui.SceneManager;
import com.gardenthyme.gui.utils.GardenDataLoader;
import com.gardenthyme.models.Pest;
import com.gardenthyme.models.Plant;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Xpedia {
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
    private Label title;
    @FXML
    private Button toggleButton;

    private boolean showingPlants = true;
    private int targetGardenID = 0;
    private int targetBoxID = 0;

    public void initialize(){
        GardenDataLoader.initializeConnection();
        
        targetBoxID = SceneManager.getTargetData();
        targetGardenID = SceneManager.getCurrentGardenID();
        
        System.out.println("Xpedia initialized - Garden: " + targetGardenID + ", Box: " + targetBoxID);
        
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
        titleBar.setSpacing(10);
        
        Button backButton = new Button("← Back");
        backButton.setStyle(
            "-fx-background-color: #D81B60;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12 5 12;"
        );
        backButton.setOnAction(e -> {
            try {
                SceneManager.setTargetData(targetGardenID);
                SceneManager.setPage("garden");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        
        if (title == null) {
            title = new Label();
        }
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#4A0025"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        if (toggleButton == null) {
            toggleButton = new Button();
        }
        toggleButton.setStyle(
            "-fx-background-color: #E91E63;" +
            "-fx-text-fill: white;" +
            "-fx-font-size:12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12 5 12;"
        );
        toggleButton.setOnAction(e -> handleToggle());
        
        titleBar.getChildren().clear();
        titleBar.getChildren().addAll(backButton, title, spacer, toggleButton);
    }

    private HBox generatePlantCard(Plant plant) {
        HBox box = new HBox();
        box.prefWidthProperty().bind(contentVBox.prefWidthProperty());
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(10);
        box.setPadding(new Insets(8, 15, 8, 15));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20;");
        box.setEffect(new javafx.scene.effect.DropShadow(3, Color.rgb(216, 27, 96, 0.1)));

        VBox names = new VBox();
        names.setSpacing(3);
        names.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(names, Priority.ALWAYS);

        Label commonName = new Label(plant.getCommonName());
        commonName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        commonName.setTextFill(Color.web("#4A0025"));

        Label scienceName = new Label(plant.getScientificName());
        scienceName.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        scienceName.setTextFill(Color.web("#D81B60"));

        names.getChildren().addAll(commonName, scienceName);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonContainer = new HBox(8);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        Button addToGarden = new Button("+ Add to Garden");
        addToGarden.setStyle(
            "-fx-background-color: #F48FB1;" +
            "-fx-text-fill: #4A0025;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12 5 12;"
        );
        
        addToGarden.setOnAction(e -> {
            if (targetGardenID > 0 && targetBoxID > 0) {
                GardenDataLoader.initializeConnection();
                
                if (GardenDataLoader.isPlantInBox(plant.getPlantID(), targetGardenID, targetBoxID)) {
                    showAlert("Cannot Add", plant.getCommonName() + " is already in this garden box!", Alert.AlertType.WARNING);
                    return;
                }
                
                boolean success = GardenDataLoader.addPlantToGardenBox(plant.getPlantID(), targetGardenID, targetBoxID);
                if (success) {
                    showAlert("Success", plant.getCommonName() + " added to Box " + targetBoxID + "!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to add plant. Please try again.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Info", "Click 'Add Plant' from a garden box to add plants directly.", Alert.AlertType.INFORMATION);
            }
        });

        buttonContainer.getChildren().add(addToGarden);
        box.getChildren().addAll(names, spacer, buttonContainer);
        
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 20;");
        });
        
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20;");
        });

        box.setOnMouseClicked(e -> {
            try {
                SceneManager.setTargetData(plant.getPlantID());
                SceneManager.setPage("dbPlantInfo");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        return box;
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

    private void showLinkPestToPlantDialog(Pest pest) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Link Pest to Plant");
        dialog.setHeaderText("Link " + pest.getPestName() + " to a plant");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");

        VBox form = new VBox();
        form.setSpacing(10);
        form.setPadding(new Insets(10));

        Label infoLabel = new Label("Select a plant to associate with this pest:");
        infoLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        infoLabel.setTextFill(Color.web("#4A0025"));

        ComboBox<String> plantCombo = new ComboBox<>();
        plantCombo.setPromptText("Select Plant");
        plantCombo.setStyle("-fx-background-radius: 10; -fx-padding: 8;");

        // Load plants that are not already linked to this pest
        try (Connection conn = DatabaseConnection.getConnection()) {
            PlantDAO plantDAO = new PlantDAO();
            PestDAO pestDAO = new PestDAO();
            List<Plant> allPlants = plantDAO.getAllPlants(conn);
            List<Integer> linkedPlantIDs = pestDAO.getPlantsByPest(pest.getPestName(), conn);
            
            for (Plant plant : allPlants) {
                if (!linkedPlantIDs.contains(plant.getPlantID())) {
                    plantCombo.getItems().add(plant.getCommonName() + " (" + plant.getScientificName() + ")");
                    // Store plant ID as user data
                    plantCombo.setUserData(new java.util.HashMap<Integer, Integer>());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        form.getChildren().addAll(infoLabel, plantCombo);
        dialogPane.setContent(form);

        ButtonType linkButton = new ButtonType("Link", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(linkButton, cancelButton);

        Button linkBtn = (Button) dialogPane.lookupButton(linkButton);
        linkBtn.setStyle("-fx-background-color: #F48FB1; -fx-text-fill: #4A0025; -fx-font-weight: bold;");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == linkButton) {
            String selectedPlant = plantCombo.getValue();
            if (selectedPlant == null) {
                showAlert("Error", "Please select a plant to link.", Alert.AlertType.ERROR);
                return;
            }

            // Extract plant ID from selection
            try (Connection conn = DatabaseConnection.getConnection()) {
                PlantDAO plantDAO = new PlantDAO();
                List<Plant> allPlants = plantDAO.getAllPlants(conn);
                String plantCommonName = selectedPlant.split(" \\(")[0];
                
                Plant targetPlant = null;
                for (Plant plant : allPlants) {
                    if (plant.getCommonName().equals(plantCommonName)) {
                        targetPlant = plant;
                        break;
                    }
                }
                
                if (targetPlant != null) {
                    PestDAO pestDAO = new PestDAO();
                    pestDAO.linkPestToPlant(pest.getPestName(), targetPlant.getPlantID(), conn);
                    showAlert("Success", pest.getPestName() + " linked to " + targetPlant.getCommonName() + "!", Alert.AlertType.INFORMATION);
                    loadPestipedia(); // Refresh the view
                } else {
                    showAlert("Error", "Could not find the selected plant.", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to link pest to plant.", Alert.AlertType.ERROR);
            }
        }
    }

    private HBox generatePestCard(Pest pest) {
        HBox box = new HBox();
        box.prefWidthProperty().bind(contentVBox.prefWidthProperty());
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(10);
        box.setPadding(new Insets(8, 15, 8, 15));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20;");
        box.setEffect(new javafx.scene.effect.DropShadow(3, Color.rgb(216, 27, 96, 0.1)));

        VBox names = new VBox();
        names.setSpacing(3);
        names.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(names, Priority.ALWAYS);

        Label pestName = new Label(pest.getPestName());
        pestName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        pestName.setTextFill(Color.web("#4A0025"));

        Label pesticide = new Label("Treatment: " + pest.getEffectivePesticides());
        pesticide.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        pesticide.setTextFill(Color.web("#D81B60"));

        names.getChildren().addAll(pestName, pesticide);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonContainer = new HBox(8);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        Button linkPest = new Button("🔗 Link to Plant");
        linkPest.setStyle(
            "-fx-background-color: #F48FB1;" +
            "-fx-text-fill: #4A0025;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12 5 12;"
        );
        linkPest.setOnAction(e -> {
            showLinkPestToPlantDialog(pest);
        });

        buttonContainer.getChildren().add(linkPest);
        box.getChildren().addAll(names, spacer, buttonContainer);
        
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 20;");
        });
        
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20;");
        });

        box.setOnMouseClicked(e -> {
            try {
                SceneManager.setTargetPestName(pest.getPestName());
                SceneManager.setTargetData(pest.getPestName().hashCode());
                SceneManager.setPage("pestInfo");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        return box;
    }

    @FXML
    public void handleToggle() {
        if (showingPlants) {
            // Currently showing plants - switch to pests
            showingPlants = false;
            toggleButton.setText("🌿 Show Plants");
            loadPestipedia();
        } else {
            // Currently showing pests - switch to plants
            showingPlants = true;
            toggleButton.setText("🐛 Show Pests");
            loadPlantipedia();
        }
    }

    public void loadPlantipedia() {
        if (title != null) title.setText("🌿 Plantipedia");
        contentVBox.getChildren().clear();
        
        // Update state to reflect we're showing plants
        showingPlants = true;
        toggleButton.setText("🐛 Show Pests");
        
        GardenDataLoader.initializeConnection();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            PlantDAO dao = new PlantDAO();
            List<Plant> plants = dao.getAllPlants(conn);
            for (Plant plant : plants) {
                contentVBox.getChildren().add(generatePlantCard(plant));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading plants: " + e.getMessage());
            errorLabel.setTextFill(Color.RED);
            contentVBox.getChildren().add(errorLabel);
        }
    }

    public void loadPestipedia() {
        if (title != null) title.setText("🐛 Pestipedia");
        contentVBox.getChildren().clear();
        
        // Update state to reflect we're showing pests
        showingPlants = false;
        toggleButton.setText("🌿 Show Plants");
        
        GardenDataLoader.initializeConnection();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            PestDAO dao = new PestDAO();
            List<Pest> pests = dao.getAllPests(conn);
            for (Pest pest : pests) {
                contentVBox.getChildren().add(generatePestCard(pest));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading pests: " + e.getMessage());
            errorLabel.setTextFill(Color.RED);
            contentVBox.getChildren().add(errorLabel);
        }
    }
}
