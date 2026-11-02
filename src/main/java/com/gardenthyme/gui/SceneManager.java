package com.gardenthyme.gui;

import java.io.IOException;

import com.gardenthyme.dao.UserDAO;
import com.gardenthyme.database.DatabaseConnection;
import com.gardenthyme.gui.controllers.Info;
import com.gardenthyme.gui.controllers.LoginSignup;
import com.gardenthyme.gui.controllers.Xpedia;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private static String previousPage = "startup";
    private static String currentPage = "startup";

    public static void setStage(Stage stage) {
        SceneManager.stage = stage;
    }

    private static int currentUserID;
    private static int currentTargetData;
    private static int prevTargetData;
    private static int currentGardenID = 0;
    private static Stage stage;
    
    private static String currentTargetPestName;
    private static String previousTargetPestName;

    public static void setPage(String s) throws IOException {
        previousPage = currentPage;
        FXMLLoader fxmlLoader = null;
        Scene scene = null;
        switch (s){
            case "startup":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/startup.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                break;
            case "login":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/loginsignup.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                LoginSignup loginController = fxmlLoader.getController();
                loginController.setLogin(true);
                loginController.setUserDAO(new UserDAO(DatabaseConnection.getConnection()));
                break;
            case "signup":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/loginsignup.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                LoginSignup signupController = fxmlLoader.getController();
                signupController.setLogin(false);
                signupController.setUserDAO(new UserDAO(DatabaseConnection.getConnection()));
                break;
            case "garden":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/garden.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                currentGardenID = currentTargetData;
                break;
            case "pestipedia":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/xpedia.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                ((Xpedia)fxmlLoader.getController()).loadPestipedia();
                break;
            case "plantipedia":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/xpedia.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                ((Xpedia)fxmlLoader.getController()).loadPlantipedia();
                break;
            case "myPlantInfo":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/info.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                Info controller = fxmlLoader.getController();
                controller.setIsInGarden(true);
                break;
            case "dbPlantInfo":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/info.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                Info dbcontroller = fxmlLoader.getController();
                dbcontroller.setIsInGarden(false);
                break;
            case "pestInfo":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/info.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                Info dbpcontroller = fxmlLoader.getController();
                dbpcontroller.setIsPest(true);
                break;
            case "profile":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/profile.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                break;
            case "adminview":
                fxmlLoader = new FXMLLoader(SceneManager.class.getResource("/fxmls/adminview.fxml"));
                scene = new Scene(fxmlLoader.load(), 400, 730);
                break;
        }
        currentPage = s;
        stage.setScene(scene);
    }

    public static Stage getStage() {
        return stage;
    }

    public static void refreshCurrentPage() throws IOException {
        setPage(currentPage);
    }

    public static int getTargetData() {
        return currentTargetData;
    }

    public static void setTargetData(int currentTargetData) {
        SceneManager.prevTargetData = SceneManager.currentTargetData;
        SceneManager.currentTargetData = currentTargetData;
    }

    public static String getTargetPestName() {
        return currentTargetPestName;
    }

    public static void setTargetPestName(String name) {
        SceneManager.previousTargetPestName = SceneManager.currentTargetPestName;
        SceneManager.currentTargetPestName = name;
    }
    
    public static String getPreviousTargetPestName() {
        return previousTargetPestName;
    }

    public static int getCurrentUserID() {
        return currentUserID;
    }

    public static void setCurrentUserID(int currentUserID) {
        SceneManager.currentUserID = currentUserID;
    }

    public static String getPreviousPage() {
        return previousPage;
    }

    public static void setPreviousPage(String previousPage) {
        SceneManager.previousPage = previousPage;
    }

    public static int getCurrentGardenID() {
        return currentGardenID;
    }

    public static void setCurrentGardenID(int gardenID) {
        SceneManager.currentGardenID = gardenID;
    }
}
