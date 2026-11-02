package com.gardenthyme.gui.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.gardenthyme.dao.PestDAO;
import com.gardenthyme.dao.PlantDAO;
import com.gardenthyme.dao.WeatherDAO;
import com.gardenthyme.database.DatabaseConnection;
import com.gardenthyme.gui.SceneManager;
import com.gardenthyme.models.Pest;
import com.gardenthyme.models.Plant;
import com.gardenthyme.models.Weather;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AdminView {

    @FXML private AnchorPane primaryPane;
    @FXML private HBox titleBar;
    @FXML private VBox baseVBox;
    @FXML private VBox contentVBox;
    @FXML private ScrollPane scrollPane;
    @FXML private Label title;

    @FXML private VBox plantEditVBox;
    @FXML private TextField cnField;
    @FXML private TextField snField;
    @FXML private TextField soilField;
    @FXML private ComboBox<String> monthCBox;
    @FXML private ComboBox<String> sunCBox;
    @FXML private Slider tempSlider;
    @FXML private Slider humiditySlider;
    @FXML private TextArea instrField;
    @FXML private Label tempLabel;
    @FXML private Label humidityLabel;
    @FXML private Button addToDBButton;
    @FXML private Button applyButton;

    @FXML private VBox pestEditVBox;
    @FXML private TextField nField;
    @FXML private FlowPane pesticidesFlow;
    @FXML private TextField addPesticideField;
    @FXML private Button addPesticideButton;
    @FXML private TextArea signsField;
    @FXML private Button applyButtonPests;

    private boolean showingPlants = true;
    private boolean existsPopup = false;
    private Button toggleBtn;  // Store reference to toggle button

    // Simple shared state so info pages know what was selected
    public static int selectedPlantID = -1;
    public static String selectedPestName = null;

    public void initialize(){
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

        // Style title bar
        titleBar.prefHeightProperty().bind(primaryPane.prefHeightProperty().multiply(0.08));
        titleBar.setStyle("-fx-background-color: #F48FB1;");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 15, 0, 15));
        titleBar.setSpacing(10);

         // Create back button
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
        addHoverAnimation(backButton);
        backButton.setOnAction(e -> {
            try {
                SceneManager.setPage("profile");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        // Style title with wrapping
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#4A0025"));
        title.setWrapText(true);
        title.setMaxWidth(180);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        // Toggle button
        toggleBtn = new Button();
        toggleBtn.setStyle(
            "-fx-background-color: #E91E63;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12 5 12;"
        );
        addHoverAnimation(toggleBtn);
        toggleBtn.setText("🐛 Show Pests");
        toggleBtn.setOnAction(e -> handleToggle());

        // Add responsive behavior for title bar
        setupResponsiveTitleBar(backButton, toggleBtn);

        titleBar.getChildren().clear();
        titleBar.getChildren().addAll(backButton, title, spacer, toggleBtn);

        // Style edit boxes
        styleEditBox(plantEditVBox);
        styleEditBox(pestEditVBox);
        // Style form fields
        styleTextField(cnField);
        styleTextField(snField);
        styleTextField(soilField);
        styleTextArea(instrField);
        styleTextArea(signsField);
        styleTextField(nField);
        styleTextField(addPesticideField);

        // Style sliders
        styleSlider(tempSlider, tempLabel);
        styleSlider(humiditySlider, humidityLabel);

       // Style combo boxes
        styleComboBox(monthCBox);
        styleComboBox(sunCBox);
        
        // Style buttons
        styleButton(applyButton, "#F48FB1", "#E91E63");
        styleButton(applyButtonPests, "#F48FB1", "#E91E63");
        styleButton(addPesticideButton, "#F48FB1", "#E91E63");
        
        plantEditVBox.setManaged(false);
        plantEditVBox.setVisible(false);
        pestEditVBox.setManaged(false);
        pestEditVBox.setVisible(false);

        loadPlantipedia();
    }
    
    private void ensureSoilTypeExists(String soilType) {
        if (soilType == null || soilType.trim().isEmpty()) {
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = "SELECT Soil_Type FROM SOIL WHERE Soil_Type = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, soilType);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    // Soil type doesn't exist, prompt user for pH value
                    Stage dialog = new Stage();
                    VBox dialogVBox = new VBox();
                    dialogVBox.setSpacing(10);
                    dialogVBox.setPadding(new Insets(15));
                    dialogVBox.setStyle("-fx-background-color: #FFFFFF;");
                    
                    Label infoLabel = new Label("New soil type detected: " + soilType);
                    infoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                    infoLabel.setTextFill(Color.web("#4A0025"));
                    infoLabel.setWrapText(true);
                    
                    Label promptLabel = new Label("Please enter the pH value for this soil type:");
                    promptLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
                    
                    TextField phField = new TextField();
                    phField.setPromptText("pH (e.g., 6.5)");
                    phField.setStyle("-fx-background-radius: 10; -fx-padding: 8;");
                    
                    VBox buttonBox = new VBox(10);
                    buttonBox.setAlignment(Pos.CENTER);
                    
                    Button confirmBtn = new Button("Add Soil Type");
                    confirmBtn.setStyle("-fx-background-color: #F48FB1; -fx-text-fill: #4A0025; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 8 15 8 15;");
                    confirmBtn.setMaxWidth(Double.MAX_VALUE);
                    
                    Button skipBtn = new Button("Skip (Default pH 6.5)");
                    skipBtn.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 8 15 8 15;");
                    skipBtn.setMaxWidth(Double.MAX_VALUE);
                    
                    confirmBtn.setOnAction(ev -> {
                        String phText = phField.getText().trim();
                        float phValue = 6.5f;
                        if (!phText.isEmpty()) {
                            try {
                                phValue = Float.parseFloat(phText);
                            } catch (NumberFormatException ex) {
                                // Use default if parsing fails
                            }
                        }
                        addSoilTypeToDatabase(soilType, phValue);
                        dialog.close();
                    });
                    
                    skipBtn.setOnAction(ev -> {
                        addSoilTypeToDatabase(soilType, 6.5f);
                        dialog.close();
                    });
                    
                    buttonBox.getChildren().addAll(confirmBtn, skipBtn);
                    dialogVBox.getChildren().addAll(infoLabel, promptLabel, phField, buttonBox);
                    
                    Scene dialogScene = new Scene(dialogVBox, 350, 280);
                    dialog.setScene(dialogScene);
                    dialog.setTitle("New Soil Type");
                    dialog.setAlwaysOnTop(true);
                    dialog.showAndWait();
                }
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void addSoilTypeToDatabase(String soilType, float pH) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO SOIL (Soil_Type, pH) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, soilType);
                pstmt.setFloat(2, pH);
                pstmt.executeUpdate();
                System.out.println("Added soil type: " + soilType + " with pH: " + pH);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("Soil type already exists: " + soilType);
            } else {
                e.printStackTrace();
            }
        }
    }

     private void setupResponsiveTitleBar(Button backButton, Button toggleBtn) {
        primaryPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            
            if (width < 400) {
                // Narrow screen, adjust text sizes
                backButton.setStyle(
                    "-fx-background-color: #D81B60;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 10px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 15;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 4 8 4 8;"
                );
                title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                title.setMaxWidth(120);
                toggleBtn.setStyle(
                    "-fx-background-color: #E91E63;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 10px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 15;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 4 8 4 8;"
                );
            } else if (width < 550) {
                // Medium screen
                backButton.setStyle(
                    "-fx-background-color: #D81B60;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 15;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 5 10 5 10;"
                );
                title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                title.setMaxWidth(160);
                toggleBtn.setStyle(
                    "-fx-background-color: #E91E63;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 15;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 5 10 5 10;"
                );
            } else {
                // Wide screen
                backButton.setStyle(
                    "-fx-background-color: #D81B60;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 15;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 5 12 5 12;"
                );
                title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                title.setMaxWidth(200);
                toggleBtn.setStyle(
                    "-fx-background-color: #E91E63;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 15;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 5 12 5 12;"
                );
            }
        });
    }
    
    @FXML
    public void backToProfile() throws IOException {
        SceneManager.setPage("profile");
    }

    
    private void addHoverAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.05); st.setToY(1.05); st.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
    }

    private void styleButton(Button button, String bgColor, String hoverColor) {
        button.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: #4A0025; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 16 8 16;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 16 8 16;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: #4A0025; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 16 8 16;"));
    }

    private void styleTextField(TextField field) {
        String base = "-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-color: #F8BBD0; -fx-border-radius: 10; -fx-border-width: 1.5; -fx-padding: 8 12 8 12; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';";
        String focused = "-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-color: #E91E63; -fx-border-radius: 10; -fx-border-width: 2; -fx-padding: 8 12 8 12; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';";
        field.setStyle(base);
        field.focusedProperty().addListener((obs, oldVal, newVal) -> field.setStyle(newVal ? focused : base));
    }

    private void styleTextArea(TextArea area) {
        String base = "-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-color: #F8BBD0; -fx-border-radius: 10; -fx-border-width: 1.5; -fx-padding: 8 12 8 12; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';";
        String focused = "-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-color: #E91E63; -fx-border-radius: 10; -fx-border-width: 2; -fx-padding: 8 12 8 12; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';";
        area.setStyle(base);
        area.focusedProperty().addListener((obs, oldVal, newVal) -> area.setStyle(newVal ? focused : base));
    }

    private void styleComboBox(ComboBox comboBox) {
        comboBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-color: #F8BBD0; -fx-border-radius: 10; -fx-border-width: 1.5; -fx-padding: 6 12 6 12; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
    }

    private void styleSlider(Slider slider, Label label) {
        slider.setStyle("-fx-control-inner-background: #F8BBD0; -fx-track-color: #F8BBD0; -fx-thumb-color: #E91E63;");
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(Color.web("#D81B60"));
    }

    private void styleEditBox(VBox box) {
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-border-color: #F48FB1; -fx-border-radius: 15; -fx-border-width: 2; -fx-padding: 15; -fx-spacing: 10;");
        box.setEffect(new DropShadow(5, Color.rgb(216, 27, 96, 0.15)));
    }

    private void closePlantEdit() {
        existsPopup = false;
        plantEditVBox.setVisible(false);
        plantEditVBox.setManaged(false);
    }

    private void closePestEdit() {
        existsPopup = false;
        pestEditVBox.setVisible(false);
        pestEditVBox.setManaged(false);
    }

    //Helper: bind slider labels so it unbinds first to avoid already bound error
    private void bindSliderLabels() {
        if (tempLabel.textProperty().isBound()) tempLabel.textProperty().unbind();
        if (humidityLabel.textProperty().isBound()) humidityLabel.textProperty().unbind();
        tempLabel.textProperty().bind(tempSlider.valueProperty().asString("%.0f°C"));
        humidityLabel.textProperty().bind(humiditySlider.valueProperty().asString("%.0f%%"));
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.initOwner(primaryPane.getScene().getWindow());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: #F48FB1; -fx-text-fill: #4A0025; -fx-font-weight: bold;");
        }
        alert.showAndWait();
    }

    private HBox generatePlantCard(Plant plant) {
        HBox box = new HBox();
        box.prefWidthProperty().bind(contentVBox.prefWidthProperty());
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(10);
        box.setPadding(new Insets(10, 15, 10, 15));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20;");
        box.setEffect(new DropShadow(3, Color.rgb(216, 27, 96, 0.1)));

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

        HBox buttonHolder = new HBox(8);
        buttonHolder.setAlignment(Pos.CENTER_RIGHT);

        Button edit = new Button("✎");
        edit.setStyle("-fx-background-color: #F48FB1; -fx-text-fill: #4A0025; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 12 5 12;");
        addHoverAnimation(edit);

        Button delete = new Button("✕");
        delete.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 12 5 12;");
        addHoverAnimation(delete);

        edit.setOnAction(e -> {
            if (!existsPopup) {
                existsPopup = true;
                Stage popup = new Stage();
                VBox popupBase = new VBox();
                Scene scene = new Scene(popupBase, 350, 600);
                popup.setScene(scene);
                popup.setTitle("Edit Plant");

                cnField.setText(plant.getCommonName());
                snField.setText(plant.getScientificName());
                soilField.setText(plant.getSoilType());
                instrField.setText(plant.getGrowthInstructions());

                setupMonthCombo();
                setupSunCombo();

                //Load existing weather from DB 
                try (Connection wConn = DatabaseConnection.getConnection()) {
                    List<Weather> existing = new WeatherDAO().getWeatherInfoByPlant(plant.getPlantID(), wConn);
                    if (!existing.isEmpty()) {
                        Weather w = existing.get(0);
                        monthCBox.setValue(w.getBestMonth());
                        sunCBox.setValue(w.getSunlight());
                        tempSlider.setValue(w.getTemperature());
                        humiditySlider.setValue(w.getHumidity());
                    } else {
                        tempSlider.setValue(20);
                        humiditySlider.setValue(60);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    tempSlider.setValue(20);
                    humiditySlider.setValue(60);
                }
                bindSliderLabels();

                
                applyButton.setOnAction(updateEvent -> {
                    Plant updated = new Plant(
                            plant.getPlantID(),
                            snField.getText().trim(),
                            cnField.getText().trim(),
                            instrField.getText().trim(),
                            soilField.getText().trim()
                    );
                    Weather weather = new Weather(
                            plant.getPlantID(),
                            (String) monthCBox.getValue(),
                            (float) tempSlider.getValue(),
                            (float) humiditySlider.getValue(),
                            (String) sunCBox.getValue()
                    );
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        // Update plant info
                        new PlantDAO().updatePlant(updated, conn);
                        
                        // Delete and re-insert weather data to ensure it's updated
                        String deleteSQL = "DELETE FROM WEATHER_DATA WHERE Plant_ID = ? AND Best_Month = ?";
                        try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                            ps.setInt(1, plant.getPlantID());
                            ps.setString(2, weather.getBestMonth());
                            ps.executeUpdate();
                        }
                        
                        // Add the updated weather data
                        new WeatherDAO().addWeatherData(weather, conn);
                        
                        showAlert("Success", "Plant updated successfully!", Alert.AlertType.INFORMATION);
                        
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Error", "Failed to update plant: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                    popup.close();
                    closePlantEdit();
                    loadPlantipedia();
                });

                popupBase.getChildren().add(plantEditVBox);
                plantEditVBox.setVisible(true);
                plantEditVBox.setManaged(true);
                popupBase.setSpacing(10);
                popupBase.setPadding(new Insets(15));
                popup.initOwner(primaryPane.getScene().getWindow());
                popup.initOwner(primaryPane.getScene().getWindow());
                popup.setAlwaysOnTop(false);
                popup.toFront();
                popup.toFront();
                popup.setResizable(false);
                popup.setOnCloseRequest(closeEvent -> closePlantEdit());
                popup.show();
            }
        });

        delete.setOnAction(e -> {
            if (!existsPopup) {
                existsPopup = true;
                Stage popup = new Stage();
                VBox popupBase = new VBox();
                Scene scene = new Scene(popupBase, 280, 200);
                popup.setScene(scene);
                popup.setTitle("Confirm Delete");

                popupBase.setAlignment(Pos.CENTER);
                popupBase.setSpacing(15);
                popupBase.setPadding(new Insets(20));
                popupBase.setStyle("-fx-background-color: #FFFFFF;");

                Label question = new Label("Delete " + plant.getCommonName() + "?");
                question.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                question.setTextFill(Color.web("#4A0025"));

                Label info = new Label("This action cannot be undone.");
                info.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
                info.setTextFill(Color.web("#D81B60"));

                HBox buttonsHBox = new HBox(15);
                buttonsHBox.setAlignment(Pos.CENTER);

                Button confirmDelete = new Button("Yes");
                confirmDelete.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 6 20 6 20;");
                addHoverAnimation(confirmDelete);

                Button cancelDelete = new Button("Cancel");
                cancelDelete.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 6 20 6 20;");
                addHoverAnimation(cancelDelete);

                confirmDelete.setOnAction(confirmEvent -> {
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        new PlantDAO().deletePlant(plant.getPlantID(), conn);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    popup.close();
                    existsPopup = false;
                    loadPlantipedia();
                });

                cancelDelete.setOnAction(cancelEvent -> {
                    popup.close();
                    existsPopup = false;
                });

                buttonsHBox.getChildren().addAll(confirmDelete, cancelDelete);
                popupBase.getChildren().addAll(question, info, buttonsHBox);
                popup.setAlwaysOnTop(false);
                popup.setResizable(false);
                popup.setOnCloseRequest(closeEvent -> existsPopup = false);
                popup.show();
            }
        });

        buttonHolder.getChildren().addAll(edit, delete);
        box.getChildren().addAll(names, buttonHolder);

        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 20;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20;"));

        
        box.setOnMouseClicked(e -> {
            selectedPlantID = plant.getPlantID();
            SceneManager.setTargetData(plant.getPlantID());
            try {
                SceneManager.setPage("dbPlantInfo");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        return box;
    }

    private HBox generatePestCard(Pest pest) {
        HBox box = new HBox();
        box.prefWidthProperty().bind(contentVBox.prefWidthProperty());
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(10);
        box.setPadding(new Insets(10, 15, 10, 15));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20;");
        box.setEffect(new DropShadow(3, Color.rgb(216, 27, 96, 0.1)));

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

        HBox buttonHolder = new HBox(8);
        buttonHolder.setAlignment(Pos.CENTER_RIGHT);

        Button edit = new Button("✎");
        edit.setStyle("-fx-background-color: #F48FB1; -fx-text-fill: #4A0025; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 12 5 12;");
        addHoverAnimation(edit);

        Button delete = new Button("✕");
        delete.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 5 12 5 12;");
        addHoverAnimation(delete);

        edit.setOnAction(e -> {
            if (!existsPopup) {
                existsPopup = true;

                Pest fullPest;
                try (Connection conn = DatabaseConnection.getConnection()) {
                    fullPest = new PestDAO().getPestByID(pest.getPestName(), conn);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    existsPopup = false;
                    return;
                }
                if (fullPest == null) { existsPopup = false; return; }

                Stage popup = new Stage();
                VBox popupBase = new VBox();
                Scene scene = new Scene(popupBase, 350, 500);
                popup.setScene(scene);
                popup.setTitle("Edit Pest");

                nField.setText(fullPest.getPestName());
                signsField.setText(fullPest.getSigns() != null ? String.join("\n", fullPest.getSigns()) : "");

                pesticidesFlow.getChildren().clear();
                String[] pesticides = fullPest.getEffectivePesticides().split(",");
                for (String p : pesticides) {
                    if (!p.trim().isEmpty()) {
                        pesticidesFlow.getChildren().add(generatePesticideCard(p.trim()));
                    }
                }

                addPesticideButton.setOnAction(addEvent -> {
                    if (!addPesticideField.getText().trim().isEmpty()) {
                        pesticidesFlow.getChildren().add(generatePesticideCard(addPesticideField.getText().trim()));
                        addPesticideField.setText("");
                    }
                });

                applyButtonPests.setOnAction(updateEvent -> {
                    // Collect pesticide names from the cards currently in the flow
                    String updatedPesticides = pesticidesFlow.getChildren().stream()
                            .filter(n -> n instanceof HBox)
                            .map(n -> (Label) ((HBox) n).getChildren().get(0))
                            .map(Label::getText)
                            .collect(Collectors.joining(", "));

                    // Collect signs (one per line)
                    List<String> updatedSigns = Arrays.stream(signsField.getText().split("\n"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());

                    Pest updated = new Pest(
                            fullPest.getPestName(),
                            updatedPesticides,
                            null,           // target plants unchanged
                            updatedSigns
                    );

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        new PestDAO().updatePest(updated, conn);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    popup.close();
                    existsPopup = false;
                    loadPestipedia();
                });

                popupBase.getChildren().add(pestEditVBox);
                pestEditVBox.setVisible(true);
                pestEditVBox.setManaged(true);
                popupBase.setSpacing(10);
                popupBase.setPadding(new Insets(15));
                popup.setAlwaysOnTop(false);
                popup.setResizable(false);
               popup.setOnCloseRequest(closeEvent -> {
                    existsPopup = false;
                    pestEditVBox.setVisible(false);
                });
                    popup.show();
            }
        });

        delete.setOnAction(e -> {
            if (!existsPopup) {
                existsPopup = true;
                Stage popup = new Stage();
                VBox popupBase = new VBox();
                Scene scene = new Scene(popupBase, 280, 200);
                popup.setScene(scene);
                popup.setTitle("Confirm Delete");

                popupBase.setAlignment(Pos.CENTER);
                popupBase.setSpacing(15);
                popupBase.setPadding(new Insets(20));
                popupBase.setStyle("-fx-background-color: #FFFFFF;");

                Label question = new Label("Delete " + pest.getPestName() + "?");
                question.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                question.setTextFill(Color.web("#4A0025"));

                Label info = new Label("This action cannot be undone.");
                info.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
                info.setTextFill(Color.web("#D81B60"));

                HBox buttonsHBox = new HBox(15);
                buttonsHBox.setAlignment(Pos.CENTER);

                Button confirmDelete = new Button("Yes");
                confirmDelete.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 6 20 6 20;");
                addHoverAnimation(confirmDelete);

                Button cancelDelete = new Button("Cancel");
                cancelDelete.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 6 20 6 20;");
                addHoverAnimation(cancelDelete);

                confirmDelete.setOnAction(confirmEvent -> {
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        new PestDAO().deletePest(pest.getPestName(), conn);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    popup.close();
                    existsPopup = false;
                    loadPestipedia();
                });

                cancelDelete.setOnAction(cancelEvent -> {
                    popup.close();
                    existsPopup = false;
                });
                buttonsHBox.getChildren().addAll(confirmDelete, cancelDelete);
                popupBase.getChildren().addAll(question, info, buttonsHBox);
                popup.setAlwaysOnTop(true);
                popup.setResizable(false);
                popup.setOnCloseRequest(closeEvent -> existsPopup = false);
                popup.show();
            }
        });

        buttonHolder.getChildren().addAll(edit, delete);
        box.getChildren().addAll(names, buttonHolder);

       box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: #FCE4EC; -fx-background-radius: 20;");
        });
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20;");
        });

        box.setOnMouseClicked(e -> {
            selectedPestName = pest.getPestName();
            try {
                SceneManager.setPage("pestInfo");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        return box;
    }

    private HBox generatePesticideCard(String p) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(8);
         card.setStyle(
            "-fx-background-color: #FCE4EC;" +
            "-fx-background-radius: 15;" +
            "-fx-padding: 5 10 5 10;"
        );
        Label pesticideLabel = new Label(p);
        pesticideLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        pesticideLabel.setTextFill(Color.web("#4A0025"));

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
        card.getChildren().addAll(pesticideLabel, deleteCard);
        return card;
    }

    private void setupMonthCombo() {
        monthCBox.getItems().clear();
        monthCBox.getItems().addAll("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        monthCBox.setValue("January");
    }

    private void setupSunCombo() {
        sunCBox.getItems().clear();
        sunCBox.getItems().addAll("Full Sun", "Part Sun", "Bright Indirect", "Filtered", "Low");
        sunCBox.setValue("Full Sun");
    }

    @FXML
    public void handleToggle() {
        if (showingPlants) {
            showingPlants = false;
            loadPestipedia();
            toggleBtn.setText("🌿 Show Plants");
        } else {
            showingPlants = true;
            loadPlantipedia();
            toggleBtn.setText("🐛 Show Pests");
        }
    }

    private void loadPlantipedia() {
        title.setText("🌿 Plantipedia (Admin Mode)");
        contentVBox.getChildren().clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Plant> plants = new PlantDAO().getAllPlants(conn);
            for (Plant plant : plants) {
                contentVBox.getChildren().add(generatePlantCard(plant));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addToDBButton.setText("+ New Plant");
        styleButton(addToDBButton, "#E91E63", "#C2185B");
        addToDBButton.setOnAction(e -> {
            if (!existsPopup) {
                existsPopup = true;
                Stage popup = new Stage();
                VBox popupBase = new VBox();
                Scene scene = new Scene(popupBase, 350, 600);
                popup.setScene(scene);
                popup.setTitle("Add New Plant");

                cnField.setText("");
                cnField.setPromptText("Common Name");
                snField.setText("");
                snField.setPromptText("Scientific Name");
                soilField.setText("");
                soilField.setPromptText("Soil Type");
                instrField.setText("");
                instrField.setPromptText("Growth Instructions");

                setupMonthCombo();
                setupSunCombo();
                tempSlider.setValue(20);
                humiditySlider.setValue(60);
                bindSliderLabels();

                // ═══ FIX #1: IMPROVED ADD PLANT LOGIC ═══
                applyButton.setOnAction(addEvent -> {
                    String soilType = soilField.getText().trim();
                    if (!soilType.isEmpty()) {
                        // Check and ensure soil type exists before creating plant
                        ensureSoilTypeExists(soilType);
                    }
                    
                    String commonName = cnField.getText().trim();
                    String scientificName = snField.getText().trim();
                    
                    if (commonName.isEmpty() || scientificName.isEmpty()) {
                        showAlert("Error", "Common Name and Scientific Name are required!", Alert.AlertType.ERROR);
                        return;
                    }

                    Plant newPlant = new Plant(
                            scientificName,
                            commonName,
                            instrField.getText().trim(),
                            soilType
                    );
                    
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        // Add the plant
                        new PlantDAO().addPlant(newPlant, conn);
                        
                        // Check if plantID was generated
                        if (newPlant.getPlantID() == 0) {
                            showAlert("Error", "Failed to add plant. Plant ID not generated.", Alert.AlertType.ERROR);
                            System.out.println("ERROR: Plant insert failed - ID not generated");
                            return;
                        }
                        
                        System.out.println("Plant added successfully with ID: " + newPlant.getPlantID());
                        
                        // Add weather data
                        Weather weather = new Weather(
                                newPlant.getPlantID(),
                                (String) monthCBox.getValue(),
                                (float) tempSlider.getValue(),
                                (float) humiditySlider.getValue(),
                                (String) sunCBox.getValue()
                        );
                        new WeatherDAO().addWeatherData(weather, conn);
                        
                        showAlert("Success", "Plant added successfully!", Alert.AlertType.INFORMATION);
                        
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Error", "Database error: " + ex.getMessage(), Alert.AlertType.ERROR);
                        return;
                    }
                    
                    popup.close();
                    closePlantEdit();
                    loadPlantipedia();
                });

                popupBase.getChildren().add(plantEditVBox);
                plantEditVBox.setVisible(true);
                plantEditVBox.setManaged(true);
                popupBase.setSpacing(10);
                popupBase.setPadding(new Insets(15));
                popup.setAlwaysOnTop(false);
                popup.setResizable(false);
                popup.setOnCloseRequest(closeEvent -> closePlantEdit());
                popup.show();
            }
        });
    }

    private void loadPestipedia() {
        title.setText("🐛 Pestipedia (Admin Mode)");
        contentVBox.getChildren().clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Pest> pests = new PestDAO().getAllPests(conn);
            for (Pest pest : pests) {
                contentVBox.getChildren().add(generatePestCard(pest));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addToDBButton.setText("+ New Pest");
        styleButton(addToDBButton, "#E91E63", "#C2185B");
        addToDBButton.setOnAction(e -> {
            if (!existsPopup) {
                existsPopup = true;
                Stage popup = new Stage();
                VBox popupBase = new VBox();
                Scene scene = new Scene(popupBase, 350, 500);
                popup.setScene(scene);
                popup.setTitle("Add New Pest");

                nField.setText("");
                nField.setPromptText("Pest Name");
                signsField.setText("");
                signsField.setPromptText("Signs (one per line)");
                pesticidesFlow.getChildren().clear();
                addPesticideField.setText("");
                addPesticideField.setPromptText("Add pesticide");

                addPesticideButton.setOnAction(addEvent -> {
                    if (!addPesticideField.getText().trim().isEmpty()) {
                        pesticidesFlow.getChildren().add(generatePesticideCard(addPesticideField.getText().trim()));
                        addPesticideField.setText("");
                    }
                });

                applyButtonPests.setOnAction(addEvent -> {
                    // Collect pesticide names from chips
                     String pesticides = pesticidesFlow.getChildren().stream()
                            .filter(n -> n instanceof HBox)
                            .map(n -> (Label) ((HBox) n).getChildren().get(0))
                            .map(Label::getText)
                            .collect(Collectors.joining(", "));

                    // Collect signs (one per line)
                        List<String> signs = Arrays.stream(signsField.getText().split("\n"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());

                    Pest newPest = new Pest(
                            nField.getText().trim(),
                            pesticides,
                            null,   // no target plants on creation
                            signs
                    );

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        new PestDAO().addPest(newPest, conn);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    popup.close();
                    addPesticideField.setText("");
                    existsPopup = false;
                    loadPestipedia();
                });

                popupBase.getChildren().add(pestEditVBox);
                pestEditVBox.setVisible(true);
                pestEditVBox.setManaged(true);
                popupBase.setSpacing(10);
                popupBase.setPadding(new Insets(15));
                popup.setAlwaysOnTop(false);
                popup.setResizable(false);
                 popup.setOnCloseRequest(closeEvent -> {
                    existsPopup = false;
                    pestEditVBox.setVisible(false);
                });
                popup.show();
            }
        });
    }
}
