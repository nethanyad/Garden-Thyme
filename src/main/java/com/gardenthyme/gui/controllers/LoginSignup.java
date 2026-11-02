package com.gardenthyme.gui.controllers;

import java.io.IOException;

import com.gardenthyme.dao.UserDAO;
import com.gardenthyme.gui.SceneManager;
import com.gardenthyme.models.User;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class LoginSignup {
    @FXML
    private AnchorPane primaryPane;
    @FXML
    private VBox vbox;
    @FXML
    private VBox emailContainer;
    @FXML
    private VBox usernameContainer;
    @FXML
    private VBox passwordContainer;
    @FXML
    private Button submitButton;
    @FXML
    private Button switchButton;
    @FXML
    private Label title;
    @FXML
    private TextField usernameInput;
    @FXML
    private TextField emailInput;
    @FXML
    private TextField passwordInput;
    @FXML
    private Label errorLabel;
    @FXML
    private VBox roleContainer;
    @FXML
    private ChoiceBox<String> roleChoice;

    private boolean isLogin = true;
    private UserDAO userDAO;

    public void initialize(){
        // Set background
        primaryPane.setStyle("-fx-background-color: linear-gradient(to bottom, #FFFFFF, #FFFFFF);");
        
        vbox.prefWidthProperty().bind(primaryPane.prefWidthProperty());
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);
        vbox.setPadding(new Insets(100, 30, 30, 30));
        
        // Style the containers
        styleInputContainer(emailContainer);
        styleInputContainer(usernameContainer);
        styleInputContainer(passwordContainer);
        styleInputContainer(roleContainer);
        
        // Style text fields
        styleTextField(emailInput, "Email");
        styleTextField(usernameInput, "Username");
        styleTextField(passwordInput, "Password");
        
        // Style role choice box
        roleChoice.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-background-radius: 25;" +
            "-fx-border-color: #F48FB1;" +
            "-fx-border-radius: 25;" +
            "-fx-border-width: 1.5;" +
            "-fx-padding: 8 12 8 12;" +
            "-fx-font-size: 14px;" +
            "-fx-font-family: 'Segoe UI';"
        );
        
        // Style title
        title.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #D81B60;" +
            "-fx-font-family: 'Segoe UI';"
        );
        title.setEffect(new DropShadow(8, Color.rgb(216, 27, 96, 0.15)));
        
        // Style error label
        errorLabel.setStyle("-fx-text-fill: #E91E63; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        
        roleChoice.getItems().addAll("GARDENER", "ADMIN");
        roleChoice.setValue("GARDENER");
    }
    
    private void styleInputContainer(VBox container) {
        container.setSpacing(8);
        container.setPadding(new Insets(5, 0, 5, 0));
        
        // Add label to each container if not present
        if (container.getChildren().isEmpty() || !(container.getChildren().get(0) instanceof Label)) {
            String labelText = "";
            if (container == emailContainer) labelText = "📧 Email";
            else if (container == usernameContainer) labelText = "👤 Username";
            else if (container == passwordContainer) labelText = "🔒 Password";
            else if (container == roleContainer) labelText = "👑 Role";
            
            Label label = new Label(labelText);
            label.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #D81B60;" +
                "-fx-font-family: 'Segoe UI';"
            );
            container.getChildren().add(0, label);
        }
    }
    
    private void styleTextField(TextField field, String prompt) {
        field.setPromptText(prompt);
        field.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-background-radius: 25;" +
            "-fx-border-color: #F8BBD0;" +
            "-fx-border-radius: 25;" +
            "-fx-border-width: 1.5;" +
            "-fx-padding: 12 15 12 15;" +
            "-fx-font-size: 14px;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);"
        );
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-background-color: #FFFFFF;" +
                    "-fx-background-radius: 25;" +
                    "-fx-border-color: #E91E63;" +
                    "-fx-border-radius: 25;" +
                    "-fx-border-width: 2;" +
                    "-fx-padding: 12 15 12 15;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-family: 'Segoe UI';"
                );
            } else {
                field.setStyle(
                    "-fx-background-color: #FFFFFF;" +
                    "-fx-background-radius: 25;" +
                    "-fx-border-color: #F8BBD0;" +
                    "-fx-border-radius: 25;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-padding: 12 15 12 15;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-family: 'Segoe UI';"
                );
            }
        });
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setupLogin(){
        isLogin = true;
        
        switchButton.setText("✨ Don't have an account? Sign up here!");
        title.setText("Welcome Back! 🌱");
        vbox.prefHeightProperty().bind(primaryPane.prefHeightProperty().multiply(0.7));
        usernameContainer.setVisible(false);
        usernameContainer.setManaged(false);
        roleContainer.setVisible(false);
        roleContainer.setManaged(false);
        submitButton.setText("Log In");
        
        // Clear any previous error
        errorLabel.setVisible(false);
        
        styleSubmitButton(submitButton, "#F48FB1", "#E91E63");

        submitButton.setOnAction(e -> {
            String email = emailInput.getText();
            String password = passwordInput.getText();

            if (email.isEmpty() || password.isEmpty()) {
                showError("Please enter email and password");
                return;
            }

            User user = userDAO.login(email, password);

            if (user != null) {
                SceneManager.setCurrentUserID(user.getUserID());
                try {
                    SceneManager.setPage("profile");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                showError("Invalid email or password");
            }
        });

        switchButton.setOnAction(e -> {
            setupSignup();
        });
        styleSwitchButton();
    }

    public void setupSignup(){
        isLogin = false;
        
        switchButton.setText("🌸 Already have an account? Login here!");
        title.setText("Create Your Garden 🌻");
        vbox.prefHeightProperty().bind(primaryPane.prefHeightProperty().multiply(0.85));
        usernameContainer.setVisible(true);
        usernameContainer.setManaged(true);
        roleContainer.setVisible(true);
        roleContainer.setManaged(true);
        submitButton.setText("Sign Up");
        
        // Clear fields and error
        usernameInput.clear();
        emailInput.clear();
        passwordInput.clear();
        errorLabel.setVisible(false);
        
        styleSubmitButton(submitButton, "#E91E63", "#C2185B");

        submitButton.setOnAction(e -> {
            String username = usernameInput.getText().trim();
            String email = emailInput.getText().trim();
            String password = passwordInput.getText();
            String role = roleChoice.getValue();

            if (username.isEmpty()) {
                showError("Please enter a username");
                return;
            }
            if (email.isEmpty()) {
                showError("Please enter an email");
                return;
            }
            if (password.isEmpty()) {
                showError("Please enter a password");
                return;
            }
            if (password.length() < 6) {
                showError("Password must be at least 6 characters");
                return;
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);

            boolean success = userDAO.createUser(user, role);

            if (success) {
                User loggedIn = userDAO.login(email, password);
                if (loggedIn != null) {
                    SceneManager.setCurrentUserID(loggedIn.getUserID());
                }
                try {
                    SceneManager.setPage("profile");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                showError("Signup failed. Username or email may already exist.");
            }
        });

        switchButton.setOnAction(e -> {
            setupLogin();
        });
        styleSwitchButton();
    }
    
    private void styleSubmitButton(Button button, String bgColor, String hoverColor) {
        button.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-background-radius: 30;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 12 25 12 25;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"
        );
        button.prefWidthProperty().bind(primaryPane.widthProperty().multiply(0.6));
        
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: " + hoverColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-background-radius: 30;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 12 25 12 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 3);"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-background-radius: 30;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 12 25 12 25;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"
            );
        });
    }
    
    private void styleSwitchButton() {
        switchButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #D81B60;" +
            "-fx-font-size: 12px;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-cursor: hand;" +
            "-fx-underline: true;"
        );
        switchButton.setOnMouseEntered(e -> {
            switchButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #E91E63;" +
                "-fx-font-size: 12px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-cursor: hand;" +
                "-fx-underline: true;"
            );
        });
        switchButton.setOnMouseExited(e -> {
            switchButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #D81B60;" +
                "-fx-font-size: 12px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-cursor: hand;" +
                "-fx-underline: true;"
            );
        });
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #E91E63; -fx-font-size: 12px; -fx-font-weight: bold;");
    }

    public void setLogin(boolean login) {
        if (login) {
            setupLogin();
        } else {
            setupSignup();
        }
    }
}
