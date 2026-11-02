package com.gardenthyme.gui;
import javafx.fxml.*;
import javafx.stage.Stage;
import javafx.scene.*;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/fxmls/startup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 730);
        SceneManager.setStage(stage);
        stage.setTitle("Garden Thyme");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args){
        launch();
    }
}
