package com.plss;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginScreen extends VBox {
    private final TextField phoneField = new TextField();
    private final PasswordField passkeyField = new PasswordField();
    private final Button loginButton = new Button("Login");

    public LoginScreen() {
        setSpacing(20);
        setPadding(new Insets(40));
        setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Personal Finance Manager");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        phoneField.setPromptText("Phone Number");
        passkeyField.setPromptText("Passkey");
        loginButton.setMaxWidth(Double.MAX_VALUE);

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
