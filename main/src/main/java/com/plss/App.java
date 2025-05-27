package com.plss;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        // Initialize database
        DatabaseManager.initializeDatabase();
        
        // Create and show login view
        LoginScreen loginScreen = new LoginScreen();
        Scene scene = new Scene(loginScreen, 400, 600);
        stage.setScene(scene);
        stage.setTitle("Personal Finance Manager - Login");
        stage.show();
        
        // Add login button handler
        loginScreen.getLoginButton().setOnAction(e -> {
            String phone = loginScreen.getPhoneField().getText();
            String passkey = loginScreen.getPasskeyField().getText();
            User user = UserManager.login(phone, passkey);
            if (user != null) {
                showMainScreen(stage, user);
            } else {
                showAlert("Login Failed", "Invalid phone number or passkey");
            }
        });
    }
    
    private void showMainScreen(Stage stage, User user) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainScreen.fxml"));
            Parent root = loader.load();
            
            // Get controller and set user
            MainScreenController controller = loader.getController();
            controller.setUser(user);
            
            // Show main screen
            Scene scene = new Scene(root, 1024, 768);
            stage.setScene(scene);
            stage.setTitle("Personal Finance Manager - " + user.getPhone());
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load main screen");
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch();
    }
}
