package com.gardenthyme.gui.controllers;

import java.io.IOException;
import java.sql.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.gardenthyme.dao.*;
import com.gardenthyme.database.DatabaseConnection;
import com.gardenthyme.gui.SceneManager;
import com.gardenthyme.models.*;

import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Info {
    @FXML
    private AnchorPane primaryPane;
    @FXML
    private VBox baseVBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox contentVBox;
    @FXML
    private HBox titleBar;
    @FXML
    private Label commonNameLabel;
    @FXML
    private Label scienceNameLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private Text descText;
    @FXML
    private Text instructionText;
    @FXML
    private Accordion dropdownContainer;
    @FXML
    private Button newLogButton;
    @FXML
    private Button newTaskButton;
    @FXML
    private VBox newTaskVBox;
    @FXML
    private VBox newLogVBox;
    @FXML
    private ToggleButton oneTimeToggle;
    @FXML
    private ToggleButton reoccurringToggle;
    @FXML
    private TextField taskField;
    @FXML
    private DatePicker oneDatePicker;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private TextField repeatDaysField;
    @FXML
    private Button createTaskButton;
    @FXML
    private Button createLogButton;
    @FXML
    private VBox oneTimeBox;
    @FXML
    private VBox reoccurringBox;
    @FXML
    private TextField logQuantityField;
    @FXML
    private DatePicker logDatePicker;
    @FXML
    private VBox buttonContainer;
    @FXML
    private Button backButton;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private FlowPane assignedUsersFlow;
    @FXML
    private VBox gardenersListVBox;
    private boolean isPestMode = false;
    private String currentPestName;
    private int currentPlantID;
    private boolean isInGarden = false;
    private Label titleLabel;

    public void initialize() {
        // Set background
        primaryPane.setStyle("-fx-background-color: #FFFFFF;");

        // Base VBox layout
        baseVBox.prefHeightProperty().bind(primaryPane.prefHeightProperty());
        baseVBox.prefWidthProperty().bind(primaryPane.prefWidthProperty());
        baseVBox.setSpacing(0);

        // Style the scroll pane
        scrollPane.prefWidthProperty().bind(primaryPane.prefWidthProperty());
        scrollPane.prefHeightProperty().bind(primaryPane.prefHeightProperty().multiply(0.88));
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF; -fx-border-color: transparent;");

        StackPane holder = new StackPane(contentVBox);
        scrollPane.setContent(holder);
        contentVBox.prefWidthProperty().bind(primaryPane.prefWidthProperty().multiply(0.9));
        contentVBox.setAlignment(Pos.TOP_CENTER);
        contentVBox.setSpacing(15);
        contentVBox.setPadding(new Insets(20, 0, 30, 0));

        // Style the title bar
        titleBar.prefHeightProperty().bind(primaryPane.prefHeightProperty().multiply(0.1));
        titleBar.setStyle("-fx-background-color: #F48FB1;");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 15, 0, 15));
        titleBar.setSpacing(10);

        // Style the back button
        if (backButton != null) {
            styleBackButton(backButton);
            backButton.setOnAction(e -> {
                try {
                    backToGarden();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        // Style the action buttons
        styleButton(newLogButton);
        styleButton(newTaskButton);

        if (newLogButton != null)
            newLogButton.setOnAction(e -> triggerNewLog());
        if (newTaskButton != null)
            newTaskButton.setOnAction(e -> triggerNewTask());

        setupResponsiveTitleBar();

        // Style the plant info cards
        styleInfoCards();

        // Style the image view and set up centered layout
        setupCenteredImage();

        // Initialize popup containers
        if (newTaskVBox != null) {
            newTaskVBox.setVisible(false);
            newTaskVBox.setManaged(false);
        }
        if (newLogVBox != null) {
            newLogVBox.setVisible(false);
            newLogVBox.setManaged(false);
        }

        // Apply global accordion styling after scene is ready
        primaryPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyGlobalAccordionStyle();
            }
        });

        // Also try to apply if scene is already available
        if (primaryPane.getScene() != null) {
            applyGlobalAccordionStyle();
        }

        // Make sure only digits can be put in quantity field
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            // Regular expression: matches an empty string or any sequence of digits
            if (text.matches("\\d*")) {
                return change;
            }
            return null; // Veto the change
        };

        logQuantityField.setTextFormatter(new TextFormatter<>(filter));
        repeatDaysField.setTextFormatter(new TextFormatter<>(filter));
    }

    private void styleBackButton(Button button) {
        button.setStyle(
                "-fx-background-color: #D81B60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 12 5 12;");
        addHoverAnimation(button);
    }

    private void styleButton(Button button) {
        if (button != null) {
            button.setStyle(
                    "-fx-background-color: #E91E63;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 5 12 5 12;");
            addHoverAnimation(button);
        }
    }

    private void applyGlobalAccordionStyle() {
        if (primaryPane.getScene() == null)
            return;

        // Apply CSS to the scene to style all accordions
        String globalStyle = ".titled-pane > .title {" +
                "    -fx-background-color: #FCE4EC !important;" +
                "    -fx-background-radius: 10 10 0 0 !important;" +
                "    -fx-border-color: #F48FB1 !important;" +
                "    -fx-border-width: 1 1 0 1 !important;" +
                "    -fx-border-radius: 10 10 0 0 !important;" +
                "    -fx-padding: 8 10 8 10 !important;" +
                "}" +
                ".titled-pane > .title > .arrow-button .arrow {" +
                "    -fx-background-color: #D81B60 !important;" +
                "}" +
                ".titled-pane > .content {" +
                "    -fx-background-color: #FFFFFF !important;" +
                "    -fx-border-color: #F48FB1 !important;" +
                "    -fx-border-width: 0 1 1 1 !important;" +
                "    -fx-border-radius: 0 0 10 10 !important;" +
                "    -fx-padding: 10 !important;" +
                "}" +
                ".titled-pane {" +
                "    -fx-background-color: transparent !important;" +
                "    -fx-text-fill: #4A0025 !important;" +
                "}";

        primaryPane.getScene().getRoot().setStyle(globalStyle);
    }

    private void styleTitledPane(TitledPane pane) {
        pane.setStyle("-fx-background-color: transparent;");
        pane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    pane.applyCss();
                    pane.layout();
                });
            }
        });
        pane.applyCss();
    }

    private void setupCenteredImage() {
        HBox imageContainer = new HBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(10, 0, 10, 0));
        
        styleImageView();
        
        imageContainer.getChildren().add(imageView);
    }
    
    private void setupResponsiveTitleBar() {
        primaryPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            
            if (width < 380) {
                if (backButton != null) {
                    backButton.setText("←");
                    backButton.setMinWidth(35);
                    backButton.setStyle(
                            "-fx-background-color: #D81B60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 8 5 8;");
                }
                if (newLogButton != null) {
                    newLogButton.setText("📋");
                    newLogButton.setMinWidth(35);
                    newLogButton.setStyle(
                            "-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 8 5 8;");
                }
                if (newTaskButton != null) {
                    newTaskButton.setText("✓");
                    newTaskButton.setMinWidth(35);
                    newTaskButton.setStyle(
                            "-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 8 5 8;");
                }
                if (titleLabel != null) {
                    titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                    titleLabel.setMaxWidth(180);
                }
            } else if (width < 480) {
                if (backButton != null) {
                    backButton.setText("← Back");
                    backButton.setMinWidth(55);
                    backButton.setStyle(
                            "-fx-background-color: #D81B60; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 10 5 10;");
                }
                if (newLogButton != null) {
                    newLogButton.setText("+Log");
                    newLogButton.setMinWidth(45);
                }
                if (newTaskButton != null) {
                    newTaskButton.setText("+Task");
                    newTaskButton.setMinWidth(45);
                }
                if (titleLabel != null) {
                    titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                    titleLabel.setMaxWidth(220);
                }
            } else {
                if (backButton != null) {
                    backButton.setText("← Back");
                    backButton.setMinWidth(70);
                    backButton.setStyle(
                            "-fx-background-color: #D81B60; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 12 5 12;");
                }
                if (newLogButton != null) {
                    newLogButton.setText("📋 New Log");
                    newLogButton.setMinWidth(70);
                }
                if (newTaskButton != null) {
                    newTaskButton.setText("✓ New Task");
                    newTaskButton.setMinWidth(70);
                }
                if (titleLabel != null) {
                    titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                    titleLabel.setMaxWidth(280);
                }
            }
        });
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
    
    private void styleInfoCards() {
        if (commonNameLabel != null) {
            commonNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
            commonNameLabel.setTextFill(Color.web("#4A0025"));
            commonNameLabel.setWrapText(true);
            commonNameLabel.setAlignment(Pos.CENTER);
        }
        
        if (scienceNameLabel != null) {
            scienceNameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
            scienceNameLabel.setTextFill(Color.web("#D81B60"));
            scienceNameLabel.setWrapText(true);
            scienceNameLabel.setAlignment(Pos.CENTER);
        }
        
        if (descText != null) {
            descText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
            descText.setFill(Color.web("#4A0025"));
            descText.setWrappingWidth(300);
        }
        
        if (instructionText != null) {
            instructionText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            instructionText.setFill(Color.web("#4A0025"));
            instructionText.setWrappingWidth(300);
        }
    }
    
    private void styleImageView() {
        try {
            Image plantImage = new Image(getClass().getResourceAsStream("/images/logo.png"));
            if (plantImage != null && !plantImage.isError()) {
                imageView.setImage(plantImage);
            } else {
                imageView.setImage(null);
                imageView.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 50;");
            }
        } catch (Exception e) {
            imageView.setImage(null);
            imageView.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 50;");
        }
        
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setColor(Color.rgb(216, 27, 96, 0.25));
        imageView.setEffect(shadow);
    }

    private HBox generatePlantCard(Plant plant) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(10);
        box.setPadding(new Insets(10, 15, 10, 15));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;");
        box.setEffect(new DropShadow(3, Color.rgb(216, 27, 96, 0.1)));
        box.prefWidthProperty().bind(contentVBox.prefWidthProperty());

        VBox names = new VBox();
        names.setSpacing(3);
        names.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(names, Priority.ALWAYS);

        Label commonName = new Label(plant.getCommonName());
        commonName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        commonName.setTextFill(Color.web("#4A0025"));

        Label scienceName = new Label(plant.getScientificName());
        scienceName.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        scienceName.setTextFill(Color.web("#D81B60"));

        names.getChildren().addAll(commonName, scienceName);

        Button unlinkButton = new Button("✕ Unlink");
        unlinkButton.setStyle(
                "-fx-background-color: #FFCDD2;" +
                        "-fx-text-fill: #C62828;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 4 10 4 10;");
        addHoverAnimation(unlinkButton);
        unlinkButton.setOnAction(e -> {
            showUnlinkConfirmation(plant);
        });

        box.getChildren().addAll(names, unlinkButton);
        
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 15;");
        });
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;");
        });
        
        return box;
    }

    private HBox generatePestCardForPlant(Pest pest) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(10);
        box.setPadding(new Insets(10, 15, 10, 15));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;");
        box.setEffect(new DropShadow(3, Color.rgb(216, 27, 96, 0.1)));
        box.prefWidthProperty().bind(contentVBox.prefWidthProperty());

        VBox names = new VBox();
        names.setSpacing(3);
        names.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(names, Priority.ALWAYS);

        Label pestName = new Label(pest.getPestName());
        pestName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        pestName.setTextFill(Color.web("#4A0025"));

        Label pesticide = new Label("Treatment: " + pest.getEffectivePesticides());
        pesticide.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        pesticide.setTextFill(Color.web("#D81B60"));

        names.getChildren().addAll(pestName, pesticide);

        Button unlinkButton = new Button("✕ Unlink");
        unlinkButton.setStyle(
                "-fx-background-color: #FFCDD2;" +
                        "-fx-text-fill: #C62828;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 4 10 4 10;");
        addHoverAnimation(unlinkButton);
        unlinkButton.setOnAction(e -> {
            showUnlinkPestFromPlantConfirmation(pest);
        });

        box.getChildren().addAll(names, unlinkButton);
        
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 15;");
        });
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;");
        });
        
        return box;
    }

    private void showUnlinkConfirmation(Plant plant) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unlink Plant");
        alert.setHeaderText("Unlink " + plant.getCommonName() + " from " + currentPestName + "?");
        alert.setContentText("This will remove the association between this pest and the plant.");

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
            try (Connection conn = DatabaseConnection.getConnection()) {
                PestDAO pestDAO = new PestDAO();
                pestDAO.unlinkPestFromPlant(currentPestName, plant.getPlantID(), conn);
                showAlert("Success", plant.getCommonName() + " unlinked from " + currentPestName + ".",
                        Alert.AlertType.INFORMATION);
                refreshPestInfo();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to unlink plant.", Alert.AlertType.ERROR);
            }
        }
    }

    private void showUnlinkPestFromPlantConfirmation(Pest pest) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unlink Pest");
        alert.setHeaderText("Unlink " + pest.getPestName() + " from this plant?");
        alert.setContentText("This will remove the association between this pest and the plant.");

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
            try (Connection conn = DatabaseConnection.getConnection()) {
                PestDAO pestDAO = new PestDAO();
                pestDAO.unlinkPestFromPlant(pest.getPestName(), currentPlantID, conn);
                showAlert("Success", pest.getPestName() + " unlinked from this plant.", Alert.AlertType.INFORMATION);
                refreshPlantInfo();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to unlink pest.", Alert.AlertType.ERROR);
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
        addHoverAnimation(okButton);
        alert.showAndWait();
    }

    private void refreshPestInfo() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PestDAO dao = new PestDAO();
            Pest pest = dao.getPestByID(currentPestName, conn);
            if (pest != null) {
                commonNameLabel.setText(pest.getPestName());
                scienceNameLabel.setText("Pesticide: " + pest.getEffectivePesticides());
                descText.setText(
                        "Signs: " + String.join(", ", pest.getSigns() != null ? pest.getSigns() : List.of("N/A")));
                instructionText.setText("Target Plants: "
                        + String.join(", ", pest.getTargetPlants() != null ? pest.getTargetPlants() : List.of("N/A")));

                TitledPane linkedPlantsPane = (TitledPane) dropdownContainer.getPanes().get(0);
                VBox linkedPlantsContent = new VBox();
                linkedPlantsContent.setSpacing(8);
                linkedPlantsContent.setPadding(new Insets(10));
                
                List<Integer> plantIDs = dao.getPlantsByPest(currentPestName, conn);
                if (plantIDs.isEmpty()) {
                    Label emptyLabel = new Label("No plants linked to this pest yet.");
                    emptyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
                    emptyLabel.setTextFill(Color.web("#9E9E9E"));
                    linkedPlantsContent.getChildren().add(emptyLabel);
                } else {
                    PlantDAO plantDAO = new PlantDAO();
                    WeatherDAO wdao = new WeatherDAO();
                    for (int plantID : plantIDs) {
                        Plant plant = plantDAO.getPlantByID(plantID, conn);
                        if (plant != null) {
                            linkedPlantsContent.getChildren().add(generatePlantCard(plant));
                            List<Weather> wList = wdao.getWeatherInfoByPlant(plantID, conn);
                            if (!wList.isEmpty()) {
                                Weather w = wList.get(0);
                                HBox weatherRow = new HBox(8);
                                weatherRow.setAlignment(Pos.CENTER_LEFT);
                                weatherRow.setPadding(new Insets(0, 0, 6, 10));
                                for (String txt : new String[] {
                                        "📅 " + w.getBestMonth(),
                                        "🌡 " + (int) w.getTemperature() + "°C",
                                        "💧 " + (int) w.getHumidity() + "%",
                                        "☀ " + w.getSunlight() }) {
                                    Label chip = new Label(txt);
                                    chip.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
                                    chip.setTextFill(Color.web("#4A0025"));
                                    chip.setStyle(
                                            "-fx-background-color: #FCE4EC; -fx-background-radius: 8; -fx-padding: 3 8 3 8;");
                                    weatherRow.getChildren().add(chip);
                                }
                                linkedPlantsContent.getChildren().add(weatherRow);
                            }
                        }
                    }
                }
                linkedPlantsPane.setContent(linkedPlantsContent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshPlantInfo() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PlantDAO plantDAO = new PlantDAO();
            Plant plant = plantDAO.getPlantByID(currentPlantID, conn);
            if (plant != null) {
                commonNameLabel.setText(plant.getCommonName());
                scienceNameLabel.setText(plant.getScientificName());
                descText.setText("🌱 Soil Type: " + plant.getSoilType());
                instructionText.setText(plant.getGrowthInstructions());
                
                TitledPane linkedPestsPane = (TitledPane) dropdownContainer.getPanes().get(2);
                VBox linkedPestsContent = new VBox();
                linkedPestsContent.setSpacing(8);
                linkedPestsContent.setPadding(new Insets(10));
                
                PestDAO pestDAO = new PestDAO();
                List<String> pestNames = pestDAO.getPestsByPlant(currentPlantID, conn);
                if (pestNames.isEmpty()) {
                    Label emptyLabel = new Label("No pests linked to this plant yet.");
                    emptyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
                    emptyLabel.setTextFill(Color.web("#9E9E9E"));
                    linkedPestsContent.getChildren().add(emptyLabel);
                } else {
                    for (String pestName : pestNames) {
                        Pest pest = pestDAO.getPestByID(pestName, conn);
                        if (pest != null) {
                            linkedPestsContent.getChildren().add(generatePestCardForPlant(pest));
                        }
                    }
                }
                linkedPestsPane.setContent(linkedPestsContent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setIsPest(boolean is) {
        this.isPestMode = is;
        
        // Hide new log and task buttons for pest view
        if (newLogButton != null) {
            newLogButton.setVisible(!is);
            newLogButton.setManaged(!is);
        }
        if (newTaskButton != null) {
            newTaskButton.setVisible(!is);
            newTaskButton.setManaged(!is);
        }
        
        contentVBox.getChildren().clear();
        
        HBox imageContainer = new HBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(10, 0, 15, 0));
        styleImageView();
        imageContainer.getChildren().add(imageView);
        contentVBox.getChildren().add(imageContainer);
        
        if (is) {
            if (titleLabel != null)
                titleLabel.setText("🐛 Pest Information");
            currentPestName = SceneManager.getTargetPestName();
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                PestDAO dao = new PestDAO();
                Pest pest = dao.getPestByID(currentPestName, conn);
                if (pest != null) {
                    commonNameLabel.setText(pest.getPestName());
                    scienceNameLabel.setText("Pesticide: " + pest.getEffectivePesticides());
                    descText.setText(
                            "Signs: " + String.join(", ", pest.getSigns() != null ? pest.getSigns() : List.of("N/A")));
                    instructionText.setText("Target Plants: " + String.join(", ",
                            pest.getTargetPlants() != null ? pest.getTargetPlants() : List.of("N/A")));

                    VBox nameBox = new VBox(5);
                    nameBox.setAlignment(Pos.CENTER);
                    nameBox.setPadding(new Insets(0, 0, 10, 0));
                    nameBox.getChildren().addAll(commonNameLabel, scienceNameLabel);
                    contentVBox.getChildren().add(nameBox);
                    
                    VBox descCard = createInfoCard("📝", "Description", descText);
                    contentVBox.getChildren().add(descCard);
                    
                    dropdownContainer.getPanes().clear();
                    
                    TitledPane linkedPlantsPane = new TitledPane();
                    linkedPlantsPane.setText("🌿 Linked Plants");
                    linkedPlantsPane.setExpanded(true);
                    styleTitledPane(linkedPlantsPane);
                    
                    VBox linkedPlantsContent = new VBox();
                    linkedPlantsContent.setSpacing(8);
                    linkedPlantsContent.setPadding(new Insets(10));
                    
                    List<Integer> plantIDs = dao.getPlantsByPest(currentPestName, conn);
                    if (plantIDs.isEmpty()) {
                        Label emptyLabel = new Label("No plants linked to this pest yet.");
                        emptyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
                        emptyLabel.setTextFill(Color.web("#9E9E9E"));
                        linkedPlantsContent.getChildren().add(emptyLabel);
                    } else {
                        PlantDAO plantDAO = new PlantDAO();
                        WeatherDAO wdao = new WeatherDAO();
                        for (int plantID : plantIDs) {
                            Plant plant = plantDAO.getPlantByID(plantID, conn);
                            if (plant != null) {
                                linkedPlantsContent.getChildren().add(generatePlantCard(plant));
                                List<Weather> wList = wdao.getWeatherInfoByPlant(plantID, conn);
                                if (!wList.isEmpty()) {
                                    Weather w = wList.get(0);
                                    HBox weatherRow = new HBox(8);
                                    weatherRow.setAlignment(Pos.CENTER_LEFT);
                                    weatherRow.setPadding(new Insets(0, 0, 6, 10));
                                    for (String txt : new String[] {
                                            "📅 " + w.getBestMonth(),
                                            "🌡 " + (int) w.getTemperature() + "°C",
                                            "💧 " + (int) w.getHumidity() + "%",
                                            "☀ " + w.getSunlight() }) {
                                        Label chip = new Label(txt);
                                        chip.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
                                        chip.setTextFill(Color.web("#4A0025"));
                                        chip.setStyle(
                                                "-fx-background-color: #FCE4EC; -fx-background-radius: 8; -fx-padding: 3 8 3 8;");
                                        weatherRow.getChildren().add(chip);
                                    }
                                    linkedPlantsContent.getChildren().add(weatherRow);
                                }
                            }
                        }
                    }
                    linkedPlantsPane.setContent(linkedPlantsContent);
                    dropdownContainer.getPanes().add(linkedPlantsPane);
                    contentVBox.getChildren().add(dropdownContainer);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private VBox createInfoCard(String icon, String title, Text content) {
        VBox card = new VBox();
        card.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #F48FB1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-width: 1.5;");
        card.setEffect(new DropShadow(3, Color.rgb(216, 27, 96, 0.1)));
        card.setPadding(new Insets(15));
        card.setSpacing(10);
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(18));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#4A0025"));
        
        header.getChildren().addAll(iconLabel, titleLabel);
        
        card.getChildren().addAll(header, content);
        return card;
    }

    public void setIsInGarden(boolean in) {
        this.isInGarden = in;
        if (titleLabel != null)
            titleLabel.setText("🌿 Plant Information");
        currentPlantID = SceneManager.getTargetData();
        
        contentVBox.getChildren().clear();
        
        HBox imageContainer = new HBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(10, 0, 15, 0));
        styleImageView();
        imageContainer.getChildren().add(imageView);
        contentVBox.getChildren().add(imageContainer);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            PlantDAO dao = new PlantDAO();
            Plant plant = dao.getPlantByID(currentPlantID, conn);
            if (plant != null) {
                commonNameLabel.setText(plant.getCommonName());
                scienceNameLabel.setText(plant.getScientificName());
                descText.setText("🌱 Soil Type: " + plant.getSoilType());
                instructionText.setText(plant.getGrowthInstructions());
                
                VBox nameBox = new VBox(5);
                nameBox.setAlignment(Pos.CENTER);
                nameBox.setPadding(new Insets(0, 0, 10, 0));
                nameBox.getChildren().addAll(commonNameLabel, scienceNameLabel);
                contentVBox.getChildren().add(nameBox);
                
                VBox descCard = createInfoCard("🌱", "Soil Type", descText);
                contentVBox.getChildren().add(descCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Weather data card
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Weather> weatherList = new WeatherDAO().getWeatherInfoByPlant(currentPlantID, conn);
            if (!weatherList.isEmpty()) {
                Weather w = weatherList.get(0); // primary entry
                VBox weatherCard = new VBox(8);
                weatherCard.setStyle(
                        "-fx-background-color: #FFFFFF;" +
                                "-fx-background-radius: 15;" +
                                "-fx-border-color: #F48FB1;" +
                                "-fx-border-radius: 15;" +
                                "-fx-border-width: 1.5;");
                weatherCard.setEffect(new DropShadow(3, Color.rgb(216, 27, 96, 0.1)));
                weatherCard.setPadding(new Insets(15));

                Label weatherTitle = new Label("☀ Ideal Growing Conditions");
                weatherTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                weatherTitle.setTextFill(Color.web("#4A0025"));

                FlowPane rows = new FlowPane();
                rows.setHgap(8);
                rows.setVgap(8);
                rows.setAlignment(Pos.CENTER_LEFT);

                Label month = new Label("📅 " + w.getBestMonth());
                Label temp = new Label("🌡 " + (int) w.getTemperature() + "°C");
                Label hum = new Label("💧 " + (int) w.getHumidity() + "% humidity");
                Label sun = new Label("☀ " + w.getSunlight());

                for (Label lbl : new Label[] { month, temp, hum, sun }) {
                    lbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
                    lbl.setTextFill(Color.web("#4A0025"));
                    lbl.setStyle(
                            "-fx-background-color: #FCE4EC;" +
                                    "-fx-background-radius: 10;" +
                                    "-fx-padding: 4 10 4 10;");
                }

                rows.getChildren().addAll(month, temp, hum, sun);
                weatherCard.getChildren().addAll(weatherTitle, rows);

                contentVBox.getChildren().add(weatherCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dropdownContainer.getPanes().clear();

        if (in) {
            // Show new log and task buttons for plants in garden
            if (newLogButton != null) {
                newLogButton.setVisible(true);
                newLogButton.setManaged(true);
            }
            if (newTaskButton != null) {
                newTaskButton.setVisible(true);
                newTaskButton.setManaged(true);
            }
            
            // Growth Instructions section
            TitledPane instructionsPane = new TitledPane();
            instructionsPane.setText("🌱 Growth Instructions");
            instructionsPane.setExpanded(true);
            styleTitledPane(instructionsPane);
            
            VBox instructionsContent = new VBox();
            instructionsContent.setPadding(new Insets(12));
            instructionsContent.setSpacing(8);
            
            Label instructionsLabel = new Label();
            instructionsLabel.setText(instructionText.getText());
            instructionsLabel.setWrapText(true);
            instructionsLabel.setFont(Font.font("Segoe UI", 13));
            instructionsLabel.setTextFill(Color.web("#4A0025"));
            instructionsContent.getChildren().add(instructionsLabel);
            instructionsPane.setContent(instructionsContent);
            dropdownContainer.getPanes().add(instructionsPane);

            // Harvest Log section
            TitledPane harvestPane = new TitledPane();
            harvestPane.setText("📋 Harvest Log");
            harvestPane.setExpanded(false);
            styleTitledPane(harvestPane);
            
            VBox harvestContent = new VBox();
            harvestContent.setSpacing(8);
            harvestContent.setPadding(new Insets(10));
            
            // Load actual harvest logs from database
            try (Connection conn = DatabaseConnection.getConnection()) {
                List<HarvestLog> logs = HarvestLogDAO.getLogs(currentPlantID, conn);
                for(HarvestLog log : logs){
                    harvestContent.getChildren().add(generateLogCard(log));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            harvestPane.setContent(harvestContent);
            dropdownContainer.getPanes().add(harvestPane);

            // Linked Pests section
            TitledPane pestsPane = new TitledPane();
            pestsPane.setText("🐛 Linked Pests");
            pestsPane.setExpanded(false);
            styleTitledPane(pestsPane);
            
            VBox pestsContent = new VBox();
            pestsContent.setSpacing(8);
            pestsContent.setPadding(new Insets(10));
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                PestDAO pestDAO = new PestDAO();
                List<String> pestNames = pestDAO.getPestsByPlant(currentPlantID, conn);
                if (pestNames.isEmpty()) {
                    Label emptyLabel = new Label("No pests linked to this plant yet.");
                    emptyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
                    emptyLabel.setTextFill(Color.web("#9E9E9E"));
                    pestsContent.getChildren().add(emptyLabel);
                } else {
                    for (String pestName : pestNames) {
                        Pest pest = pestDAO.getPestByID(pestName, conn);
                        if (pest != null) {
                            pestsContent.getChildren().add(generatePestCardForPlant(pest));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            pestsPane.setContent(pestsContent);
            dropdownContainer.getPanes().add(pestsPane);

            // Tasks section
            TitledPane tasksPane = new TitledPane();
            tasksPane.setText("✓ Tasks");
            tasksPane.setExpanded(false);
            styleTitledPane(tasksPane);
            
            VBox tasksContent = new VBox();
            tasksContent.setSpacing(8);
            tasksContent.setPadding(new Insets(10));

            try (Connection conn = DatabaseConnection.getConnection()) {
                TaskDAO taskDAO = new TaskDAO(conn);
                List<Task> tasks = taskDAO.getTasksByPlant(currentPlantID);

                for (Task task : tasks) {
                    tasksContent.getChildren().add(generateTaskCard(task));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            tasksPane.setContent(tasksContent);
            dropdownContainer.getPanes().add(tasksPane);
            
            contentVBox.getChildren().add(dropdownContainer);

        } else {
            // Hide new log and task buttons for plants not in garden
            if (newLogButton != null) {
                newLogButton.setVisible(false);
                newLogButton.setManaged(false);
            }
            if (newTaskButton != null) {
                newTaskButton.setVisible(false);
                newTaskButton.setManaged(false);
            }
            
            // Plant not in garden - only show growth instructions
            TitledPane instructionsPane = new TitledPane();
            instructionsPane.setText("🌱 Growth Instructions");
            instructionsPane.setExpanded(true);
            styleTitledPane(instructionsPane);
            
            VBox instructionsContent = new VBox();
            instructionsContent.setPadding(new Insets(12));
            instructionsContent.setSpacing(8);
            
            Label instructionsLabel = new Label();
            instructionsLabel.setText(instructionText.getText());
            instructionsLabel.setWrapText(true);
            instructionsLabel.setFont(Font.font("Segoe UI", 13));
            instructionsLabel.setTextFill(Color.web("#4A0025"));
            instructionsContent.getChildren().add(instructionsLabel);
            instructionsPane.setContent(instructionsContent);
            dropdownContainer.getPanes().add(instructionsPane);
            
            contentVBox.getChildren().add(dropdownContainer);
        }

        // Add a "Add to Garden" button if not already in garden
        if (!in) {
            Button addToGardenBtn = new Button("+ Add to My Garden");
            addToGardenBtn.setStyle(
                    "-fx-background-color: #E91E63;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 10 20 10 20;");
            addToGardenBtn.setMaxWidth(200);
            addToGardenBtn.setAlignment(Pos.CENTER);
            addHoverAnimation(addToGardenBtn);
            
            HBox buttonWrapper = new HBox();
            buttonWrapper.setAlignment(Pos.CENTER);
            buttonWrapper.setPadding(new Insets(15, 0, 0, 0));
            buttonWrapper.getChildren().add(addToGardenBtn);
            
            contentVBox.getChildren().add(buttonWrapper);
            
            addToGardenBtn.setOnAction(e -> {
                showAlert("Info", "Please go to your garden and click 'Add Plant' to add this plant.",
                        Alert.AlertType.INFORMATION);
            });
        }
    }

    private VBox generateTaskCard(Task task){
        VBox taskEntry = new VBox();
        taskEntry.setAlignment(Pos.CENTER_LEFT);
        taskEntry.setSpacing(10);
        taskEntry.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-padding: 8; -fx-border-color: #F8BBD0; -fx-border-radius: 10; -fx-border-width: 1;");

        CheckBox taskCheck = new CheckBox();
        taskCheck.setStyle("-fx-cursor: hand;");
        taskCheck.setSelected(task.isCompleted());
        taskCheck.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    TaskDAO taskDAO = new TaskDAO(conn);
                    taskDAO.completeTask(task.getTaskID(), currentPlantID, taskCheck.isSelected());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        Label taskText = new Label(task.getText());
        taskText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        taskText.setTextFill(Color.web("#4A0025"));
        taskText.setMaxWidth(200);

        Label separatorLabel = new Label(" || ");
        separatorLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        separatorLabel.setTextFill(Color.web("#4A0025"));
        separatorLabel.setMinWidth(20);

        Label dueDate = new Label(String.valueOf(task.getDate()));
        dueDate.setMinWidth(100);

        Button deleteCard = new Button("✕");
        deleteCard.setMinWidth(25);
        deleteCard.setStyle(
                "-fx-background-color: #FFCDD2;" +
                        "-fx-text-fill: #C62828;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 2 6 2 6;"
        );
        deleteCard.setOnAction(e -> {
            ((VBox)taskEntry.getParent()).getChildren().remove(taskEntry);
            try (Connection conn = DatabaseConnection.getConnection()) {
                TaskDAO taskDAO = new TaskDAO(conn);
                taskDAO.deleteTask(task.getTaskID(), currentPlantID);
            } catch (SQLException x) {
                x.printStackTrace();
            }
        });

        Label assignedHeader = new Label("Assigned to: ");
        assignedHeader.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-padding: 8; -fx-border-color: #F8BBD0; -fx-border-radius: 10; -fx-border-width: 1;");

        Label assignedList = new Label("");
        assignedList.setWrapText(true);
        assignedList.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        assignedList.setTextFill(Color.web("#4A0025"));
        assignedList.setVisible(false);
        assignedList.setManaged(false);
        assignedHeader.setVisible(false);
        assignedHeader.setManaged(false);

        taskEntry.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (assignedList.isVisible()){
                    assignedHeader.setVisible(false);
                    assignedHeader.setManaged(false);
                    assignedList.setVisible(false);
                    assignedList.setManaged(false);
                    taskText.setWrapText(false);
                }else{
                    assignedHeader.setVisible(true);
                    assignedHeader.setManaged(true);
                    assignedList.setVisible(true);
                    assignedList.setManaged(true);
                    taskText.setWrapText(true);
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        TaskDAO taskDAO = new TaskDAO(conn);
                        UserDAO userDAO = new UserDAO(conn);
                        StringBuilder usernames = new StringBuilder();
                        for(Integer id :taskDAO.getAssignedUsers(task.getTaskID(), currentPlantID)){
                            usernames.append(userDAO.findByID(id).getUsername());
                            usernames.append("\n");
                        }
                        System.out.println("Usernames: " + usernames);
                        assignedList.setText(usernames.toString());
                    } catch (SQLException x) {
                        x.printStackTrace();
                    }
                }
            }
        });

        taskEntry.setId(String.valueOf(task.getTaskID()));
        HBox taskHBox = new HBox(taskCheck, taskText, separatorLabel, dueDate, deleteCard);
        taskHBox.setSpacing(4);
        taskEntry.getChildren().addAll(taskHBox, assignedHeader, assignedList);
        return taskEntry;
    }

    @FXML
    public void triggerNewLog() {
        if (newLogButton != null)
            newLogButton.setDisable(true);

        Stage popup = new Stage();
        ScrollPane popupSPane = new ScrollPane();
        VBox popupBase = new VBox();
        popupSPane.setContent(popupBase);
        popupSPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        popupSPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        newLogVBox.prefWidthProperty().bind(popupSPane.widthProperty());
        
        if (newLogVBox != null) {
            newLogVBox.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 15; -fx-spacing: 10;");
            
            Label logTitle = new Label("Add Harvest Log");
            logTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            logTitle.setTextFill(Color.web("#4A0025"));
            
            popupBase.getChildren().addAll(logTitle, newLogVBox);
            Scene scene = new Scene(popupSPane, 300, 300);
            popup.setScene(scene);

            newLogVBox.setManaged(true);
            newLogVBox.setVisible(true);

            popup.show();
            popup.setAlwaysOnTop(true);
            popup.setOnCloseRequest(windowEvent -> {
                if (newLogButton != null)
                    newLogButton.setDisable(false);
                if (newLogVBox != null) {
                    newLogVBox.setManaged(false);
                    newLogVBox.setVisible(false);
                }
            });
            
            // Setup create log button action
            if (createLogButton != null) {
                createLogButton.setOnAction(e -> {
                    System.out.println("Create harvest log for plant: " + currentPlantID);
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        HarvestLogDAO harvestDAO = new HarvestLogDAO();
                        HarvestLog newLog = new HarvestLog(currentPlantID, Date.valueOf(logDatePicker.getValue()), Integer.parseInt(logQuantityField.getText()));
                        harvestDAO.addLog(newLog, conn);
                        ((VBox) dropdownContainer.getPanes().get(1).getContent()).getChildren().add(generateLogCard(newLog));
                    } catch (SQLException x) {
                        x.printStackTrace();
                    }
                    popup.close();
                    createLogButton.setDisable(false);
                    showAlert("Success", "Harvest log added successfully!", Alert.AlertType.INFORMATION);
                });
            }
        }
    }

    @FXML
    public void triggerNewTask() {
        if (newTaskButton != null)
            newTaskButton.setDisable(true);

        Stage popup = new Stage();
        ScrollPane popupSPane = new ScrollPane();
        VBox popupBase = new VBox();
        popupSPane.setContent(popupBase);
        popupSPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        popupSPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        newTaskVBox.prefWidthProperty().bind(popupSPane.widthProperty());
        
        if (newTaskVBox != null) {
            newTaskVBox.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 15; -fx-spacing: 10;");
            
            Label taskTitle = new Label("Add New Task");
            taskTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            taskTitle.setTextFill(Color.web("#4A0025"));
            
            popupBase.getChildren().addAll(taskTitle, newTaskVBox);
            Scene scene = new Scene(popupSPane, 300, 400);
            popup.setScene(scene);

            newTaskVBox.setManaged(true);
            newTaskVBox.setVisible(true);

            // Setup toggle behavior
            if (oneTimeToggle != null && reoccurringToggle != null) {
                oneTimeToggle.setSelected(true);
                if (reoccurringBox != null) {
                    reoccurringBox.setVisible(false);
                    reoccurringBox.setManaged(false);
                }
                if (oneTimeBox != null) {
                    oneTimeBox.setVisible(true);
                    oneTimeBox.setManaged(true);
                }
                
                oneTimeToggle.setOnAction(actionEvent -> {
                    if (reoccurringBox != null) {
                        reoccurringBox.setVisible(false);
                        reoccurringBox.setManaged(false);
                    }
                    if (oneTimeBox != null) {
                        oneTimeBox.setVisible(true);
                        oneTimeBox.setManaged(true);
                    }
                });
                reoccurringToggle.setOnAction(actionEvent -> {
                    if (reoccurringBox != null) {
                        reoccurringBox.setVisible(true);
                        reoccurringBox.setManaged(true);
                    }
                    if (oneTimeBox != null) {
                        oneTimeBox.setVisible(false);
                        oneTimeBox.setManaged(false);
                    }
                });
            }

            // Setup adding users to assign

            gardenersListVBox.getChildren().clear();

            // Add cards for gardeners to be assigned
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT u.User_ID, u.Username, u.Email, m.IsOwner " +
                        "FROM USERS u " +
                        "JOIN MANAGES m ON u.User_ID = m.User_ID " +
                        "WHERE m.Garden_ID = ? " +
                        "ORDER BY m.IsOwner DESC, u.Username";

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, SceneManager.getCurrentGardenID());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int userId = rs.getInt("User_ID");
                    String username = rs.getString("Username");
                    String email = rs.getString("Email");
                    boolean isOwner = rs.getBoolean("IsOwner");

                    HBox gardenerCard = createGardenerCard(userId, username, email, isOwner);
                    gardenersListVBox.getChildren().add(gardenerCard);
                }
                rs.close();
                pstmt.close();

                if (gardenersListVBox.getChildren().isEmpty()) {
                    Label emptyLabel = new Label("No gardeners found for this garden.");
                    emptyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
                    emptyLabel.setTextFill(Color.web("#9E9E9E"));
                    gardenersListVBox.getChildren().add(emptyLabel);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Label errorLabel = new Label("Error loading gardeners: " + e.getMessage());
                errorLabel.setTextFill(Color.RED);
                newTaskVBox.getChildren().add(errorLabel);
            }

            // Setup create task button action

            createTaskButton.setOnAction(e -> {
                String taskText = taskField != null ? taskField.getText() : "";
                Task newTask = new Task();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    TaskDAO taskDAO = new TaskDAO(conn);
                    UserDAO userDAO = new UserDAO(conn);
                    Date firstDate = new Date(0,0,0); // Don't delete.
                    Date repeatUntil = null;
                    if(reoccurringBox.isVisible()){
                        repeatUntil = Date.valueOf(endDatePicker.getValue());
                        int frequency = Integer.parseInt(repeatDaysField.getText());
                        int amtOfRepeats = (int) (startDatePicker.getValue().until(endDatePicker.getValue(), ChronoUnit.DAYS)/frequency);
                        for(int i = 0; i < amtOfRepeats; i++){
                            newTask = new Task(currentPlantID, Date.valueOf(startDatePicker.getValue().plusDays((long) frequency * i)), taskText, false, Integer.parseInt(repeatDaysField.getText()), repeatUntil);
                            taskDAO.addTask(newTask);
                            int newTaskID = taskDAO.getTasksByPlant(currentPlantID).getLast().getTaskID();
                            for(Node child : assignedUsersFlow.getChildren()){
                                String username = ((Label)(((HBox) child).getChildren().getFirst())).getText();
                                if(!taskDAO.getAssignedUsers(newTask.getTaskID(), currentPlantID).contains(userDAO.findByUsername(username).getUserID())){
                                    taskDAO.assignTask(newTaskID, currentPlantID, userDAO.findByUsername(username).getUserID());
                                }
                            }
                            newTask.setTaskID(newTaskID);
                            ((VBox) dropdownContainer.getPanes().getLast().getContent()).getChildren().add(generateTaskCard(newTask));
                        }
                    }else{
                        firstDate = Date.valueOf(oneDatePicker.getValue());
                        newTask = new Task(currentPlantID, firstDate, taskText, false, Integer.parseInt(repeatDaysField.getText()), repeatUntil);
                        taskDAO.addTask(newTask);
                        int newTaskID = taskDAO.getTasksByPlant(currentPlantID).getLast().getTaskID();
                        for(Node child : assignedUsersFlow.getChildren()){
                            String username = ((Label)(((HBox) child).getChildren().getFirst())).getText();
                            if(!taskDAO.getAssignedUsers(newTask.getTaskID(), currentPlantID).contains(userDAO.findByUsername(username).getUserID())){
                                taskDAO.assignTask(newTaskID, currentPlantID, userDAO.findByUsername(username).getUserID());
                            }
                        }
                        newTask.setTaskID(newTaskID);
                        ((VBox) dropdownContainer.getPanes().getLast().getContent()).getChildren().add(generateTaskCard(newTask));
                    }
                    System.out.println("Create task for plant: " + currentPlantID + " - " + taskText);
                } catch (SQLException x) {
                    x.printStackTrace();
                }
                // Add to the task pane
                popup.close();
                newTaskButton.setDisable(false);
                showAlert("Success", "Task added successfully!", Alert.AlertType.INFORMATION);
            });

            popup.show();
            popup.setAlwaysOnTop(true);
            popup.setOnCloseRequest(windowEvent -> {
                if (newTaskButton != null)
                    newTaskButton.setDisable(false);
                if (newTaskVBox != null) {
                    newTaskVBox.setManaged(false);
                    newTaskVBox.setVisible(false);
                }
            });
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

        Button assignBtn = new Button("Assign");
        assignBtn.setStyle(
                "-fx-background-color: #FFCDD2;" +
                        "-fx-text-fill: #C62828;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 4 12 4 12;"
        );
        assignBtn.setOnAction(e -> {
            assignedUsersFlow.getChildren().add(generateUserCard(username));
        });
        card.getChildren().add(assignBtn);

        return card;
    }

    private HBox generateLogCard(HarvestLog log){
        HBox entry = new HBox();
        entry.setAlignment(Pos.CENTER_LEFT);
        entry.setSpacing(10);
        entry.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 10; -fx-padding: 8;");
        Label dateLabel = new Label("📅 " + log.getDate().toString());
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        dateLabel.setTextFill(Color.web("#4A0025"));
        Label qtyLabel = new Label("Quantity: " + String.valueOf(log.getQuantity()));
        qtyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        qtyLabel.setTextFill(Color.web("#D81B60"));
        entry.getChildren().addAll(dateLabel, qtyLabel);
        return entry;
    }

    private HBox generateUserCard(String p){
        // For picking users to assign
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(8);
        card.setStyle(
                "-fx-background-color: #FCE4EC;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 5 10 5 10;"
        );

        Label userLabel = new Label(p);
        userLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        userLabel.setTextFill(Color.web("#4A0025"));

        Button deleteCard = new Button("✕");
        deleteCard.setStyle(
                "-fx-background-color: #FFCDD2;" +
                        "-fx-text-fill: #C62828;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 2 6 2 6;"
        );
        deleteCard.setOnAction(e -> {
            ((FlowPane)card.getParent()).getChildren().remove(card);
        });
        card.getChildren().addAll(userLabel, deleteCard);
        return card;
    }

    @FXML
    public void backToGarden() throws IOException {
        String previousPage = SceneManager.getPreviousPage();

        // If coming from garden, refresh the garden data by setting the target data
        // again
        if (previousPage != null && previousPage.equals("garden")) {
            // Force refresh by setting the garden ID again
            int gardenID = SceneManager.getCurrentGardenID();
            SceneManager.setTargetData(gardenID);
        }
        
        if (previousPage != null && !previousPage.isEmpty()) {
            SceneManager.setPage(previousPage);
        } else {
            SceneManager.setPage("profile");
        }
    }
}
