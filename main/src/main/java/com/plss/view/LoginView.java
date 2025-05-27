package com.plss.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginView extends VBox {
    private TextField phoneField;
    private PasswordField passkeyField;
    private Button loginButton;

    public LoginView() {
        setupUI();
    }

    private void setupUI() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(20));
        setSpacing(10);

        Label titleLabel = new Label("Personal Finance Manager");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        phoneField.setMaxWidth(300);

        passkeyField = new PasswordField();
        passkeyField.setPromptText("Passkey");
        passkeyField.setMaxWidth(300);

        loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        getChildren().addAll(titleLabel, phoneField, passkeyField, loginButton);
    }

    public TextField getPhoneField() {
        return phoneField;
    }

    public PasswordField getPasskeyField() {
        return passkeyField;
    }

    public Button getLoginButton() {
        return loginButton;
    }
}
