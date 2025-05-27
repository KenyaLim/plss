package com.plss;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class MainApp extends Application {
    
    // Main Application
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
        // TODO: Implement main screen after login
        showAlert("Success", "Welcome " + user.getPhone());
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

// User class
class User {
    private long id;
    private String phone;
    private String passkey;
    
    public User(String phone, String passkey) {
        this.phone = phone;
        this.passkey = passkey;
    }
    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasskey() { return passkey; }
    public void setPasskey(String passkey) { this.passkey = passkey; }
}

// Transaction class
class Transaction {
    private long id;
    private long userId;
    private double amount;
    private String category;
    private String description;
    private String type; // "INCOME" or "EXPENSE"
    private String date;
    
    public Transaction(double amount, String category, String description, String type, String date) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.type = type;
        this.date = date;
    }
    
    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}

// Database Manager
class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:finance.db";
    private static Connection connection;
    
    public static void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "phone TEXT UNIQUE," +
                    "passkey TEXT)");
            
            // Create transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "amount REAL," +
                    "category TEXT," +
                    "description TEXT," +
                    "type TEXT," +
                    "date TEXT," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // Create categories table
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE)");
                    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() {
        return connection;
    }
}

// User Manager
class UserManager {
    public static User login(String phone, String passkey) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String sql = "SELECT id FROM users WHERE phone = ? AND passkey = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            pstmt.setString(2, passkey);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(phone, passkey);
                user.setId(rs.getLong("id"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean registerUser(String phone, String passkey) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String sql = "INSERT INTO users (phone, passkey) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            pstmt.setString(2, passkey);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

// Login Screen
class LoginScreen extends VBox {
    private TextField phoneField;
    private PasswordField passkeyField;
    private Button loginButton;
    
    public LoginScreen() {
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
        
        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        
        registerButton.setOnAction(e -> {
            if (phoneField.getText().isEmpty() || passkeyField.getText().isEmpty()) {
                showAlert("Error", "Please fill in all fields");
                return;
            }
            
            if (UserManager.registerUser(phoneField.getText(), passkeyField.getText())) {
                showAlert("Success", "Registration successful! Please login.");
            } else {
                showAlert("Error", "Registration failed. Phone number might already be registered.");
            }
        });
        
        getChildren().addAll(titleLabel, phoneField, passkeyField, loginButton, registerButton);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
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
