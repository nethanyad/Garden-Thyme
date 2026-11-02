package com.gardenthyme.gui.controllers;

import java.io.IOException;

import com.gardenthyme.gui.SceneManager;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class Startup {
    @FXML
    private AnchorPane primaryPane;
    @FXML
    private Button loginButton;
    @FXML
    private Button signupButton;
    @FXML
    private HBox buttonContainer;
    @FXML
    private Label title;
    @FXML
    private ImageView logo;
    @FXML
    private VBox vbox;

    public void initialize() {
        // Make VBox fill entire AnchorPane
        vbox.prefWidthProperty().bind(primaryPane.widthProperty());
        vbox.prefHeightProperty().bind(primaryPane.heightProperty());
        
        // Center everything in VBox
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(25);
        vbox.setPadding(new Insets(20));
        
        // Set background to pure white
        primaryPane.setStyle("-fx-background-color: #FFFFFF;");
        
        // Style the title
        title.setStyle(
            "-fx-font-size: 36px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #D81B60;" +
            "-fx-font-family: 'Segoe UI', 'Arial';"
        );
        title.setEffect(new DropShadow(10, Color.rgb(216, 27, 96, 0.2)));
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);
        
        // Style logo
        if (logo.getImage() != null) {
            logo.setFitWidth(120);
            logo.setFitHeight(120);
            logo.setPreserveRatio(true);
        }
        
        // Center button container
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(20);
        buttonContainer.setPadding(new Insets(10, 0, 10, 0));
        
        // Style buttons
        styleButton(loginButton, "#F48FB1", "#E91E63", "LOG IN");
        styleButton(signupButton, "#F8BBD0", "#D81B60", "SIGN UP");
        
        // Set button sizes with constraints
        loginButton.setMinWidth(120);
        loginButton.setMaxWidth(200);
        signupButton.setMinWidth(120);
        signupButton.setMaxWidth(200);
        
        // Make buttons expand with window but not too large
        loginButton.prefWidthProperty().bind(primaryPane.widthProperty().multiply(0.35));
        signupButton.prefWidthProperty().bind(primaryPane.widthProperty().multiply(0.35));
        
        // Add hover effects
        addHoverEffect(loginButton);
        addHoverEffect(signupButton);
        
        // Add animations
        addFadeInAnimation();
        
        // Set button actions
        loginButton.setOnAction(this::handleLogin);
        signupButton.setOnAction(this::handleSignup);
        
        // Add resize listener to adjust font sizes
        primaryPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            if (width < 350) {
                title.setStyle(
                    "-fx-font-size: 28px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #D81B60;" +
                    "-fx-font-family: 'Segoe UI', 'Arial';"
                );
                loginButton.setStyle(
                    "-fx-background-color: #F48FB1;" +
                    "-fx-text-fill: #4A0025;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-family: 'Segoe UI', 'Arial';" +
                    "-fx-background-radius: 30;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);"
                );
                signupButton.setStyle(
                    "-fx-background-color: #F8BBD0;" +
                    "-fx-text-fill: #4A0025;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-family: 'Segoe UI', 'Arial';" +
                    "-fx-background-radius: 30;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);"
                );
            } else {
                title.setStyle(
                    "-fx-font-size: 42px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #D81B60;" +
                    "-fx-font-family: 'Segoe UI', 'Arial';"
                );
                loginButton.setStyle(
                    "-fx-background-color: #F48FB1;" +
                    "-fx-text-fill: #4A0025;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-family: 'Segoe UI', 'Arial';" +
                    "-fx-background-radius: 30;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);"
                );
                signupButton.setStyle(
                    "-fx-background-color: #F8BBD0;" +
                    "-fx-text-fill: #4A0025;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-family: 'Segoe UI', 'Arial';" +
                    "-fx-background-radius: 30;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);"
                );
            }
        });
    }
    
    private void styleButton(Button button, String bgColor, String hoverColor, String text) {
        button.setText(text);
        button.setPadding(new Insets(10, 25, 10, 25));
        
        button.hoverProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                button.setStyle(
                    "-fx-background-color: " + hoverColor + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-family: 'Segoe UI', 'Arial';" +
                    "-fx-background-radius: 30;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0.5, 0, 3);"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: " + bgColor + ";" +
                    "-fx-text-fill: #4A0025;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-family: 'Segoe UI', 'Arial';" +
                    "-fx-background-radius: 30;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);"
                );
            }
        });
    }
    
    private void addHoverEffect(Button button) {
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
    
    private void addFadeInAnimation() {
        vbox.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(800), vbox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
        
        title.setTranslateY(50);
        title.setOpacity(0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(600), title);
        tt.setToY(0);
        tt.play();
        FadeTransition ftTitle = new FadeTransition(Duration.millis(600), title);
        ftTitle.setFromValue(0);
        ftTitle.setToValue(1);
        ftTitle.play();
    }
    
    private void handleLogin(ActionEvent actionEvent) {
        addButtonClickAnimation(loginButton);
        try {
            SceneManager.setPage("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void handleSignup(ActionEvent actionEvent) {
        addButtonClickAnimation(signupButton);
        try {
            SceneManager.setPage("signup");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void addButtonClickAnimation(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
        st.setToX(0.95);
        st.setToY(0.95);
        st.setOnFinished(e -> {
            ScaleTransition st2 = new ScaleTransition(Duration.millis(100), button);
            st2.setToX(1.0);
            st2.setToY(1.0);
            st2.play();
        });
        st.play();
    }
}
