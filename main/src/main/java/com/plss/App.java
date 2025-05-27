package com.plss;

import com.plss.util.DatabaseUtil;
import com.plss.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Initialize database
        DatabaseUtil.initializeDatabase();

        // Create and show login view
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView, 400, 600);
        stage.setScene(scene);
        stage.setTitle("Personal Finance Manager - Login");
        stage.show();

        // Add login button handler
        loginView.getLoginButton().setOnAction(e -> {
            String phone = loginView.getPhoneField().getText();
            String passkey = loginView.getPasskeyField().getText();
            // TODO: Implement login logic
            System.out.println("Login attempt with phone: " + phone);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
