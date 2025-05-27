package com.plss;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.sql.*;

public class MainApp extends Application {
    
    // Main Application    @Override
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
    private javafx.beans.property.DoubleProperty amount;
    private javafx.beans.property.StringProperty category;
    private javafx.beans.property.StringProperty description;
    private javafx.beans.property.StringProperty type; // "INCOME" or "EXPENSE"
    private javafx.beans.property.StringProperty date;
    
    public Transaction(double amount, String category, String description, String type, String date) {
        this.amount = new javafx.beans.property.SimpleDoubleProperty(amount);
        this.category = new javafx.beans.property.SimpleStringProperty(category);
        this.description = new javafx.beans.property.SimpleStringProperty(description);
        this.type = new javafx.beans.property.SimpleStringProperty(type);
        this.date = new javafx.beans.property.SimpleStringProperty(date);
    }
    
    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    
    public double getAmount() { return amount.get(); }
    public void setAmount(double value) { amount.set(value); }
    public javafx.beans.property.DoubleProperty amountProperty() { return amount; }
    
    public String getCategory() { return category.get(); }
    public void setCategory(String value) { category.set(value); }
    public javafx.beans.property.StringProperty categoryProperty() { return category; }
    
    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public javafx.beans.property.StringProperty descriptionProperty() { return description; }
    
    public String getType() { return type.get(); }
    public void setType(String value) { type.set(value); }
    public javafx.beans.property.StringProperty typeProperty() { return type; }
    
    public String getDate() { return date.get(); }
    public void setDate(String value) { date.set(value); }
    public javafx.beans.property.StringProperty dateProperty() { return date; }
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

// Controller untuk MainScreen.fxml
class MainScreenController {
        @FXML private Label balanceLabel;
        @FXML private DatePicker startDate;
        @FXML private DatePicker endDate;
        @FXML private ComboBox<String> categoryFilter;
        @FXML private ComboBox<String> typeFilter;
        @FXML private TableView<Transaction> transactionTable;
        @FXML private TableColumn<Transaction, String> dateColumn;
        @FXML private TableColumn<Transaction, String> typeColumn;
        @FXML private TableColumn<Transaction, String> categoryColumn;
        @FXML private TableColumn<Transaction, String> descriptionColumn;
        @FXML private TableColumn<Transaction, Double> amountColumn;
        @FXML private TableColumn<Transaction, Void> actionColumn;
        @FXML private PieChart categoryPieChart;
        @FXML private LineChart<String, Number> trendLineChart;
        @FXML private ProgressBar targetProgress;
        @FXML private Label targetLabel;

        private User currentUser;
        private double spendingTarget = 1000000; // Default target 1 juta

        public void initialize() {
            setupTable();
            setupFilters();
            updateTransactions();
            updateCharts();
        }

        public void setUser(User user) {
            this.currentUser = user;
            updateAll();
        }

        private void setupTable() {
            dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
            typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());
            categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
            descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());
            amountColumn.setCellValueFactory(data -> data.getValue().amountProperty().asObject());

            // Setup action column
            actionColumn.setCellFactory(col -> new TableCell<>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Hapus");
                private final HBox buttons = new HBox(5, editButton, deleteButton);

                {
                    editButton.setOnAction(e -> handleEdit(getTableRow().getItem()));
                    deleteButton.setOnAction(e -> handleDelete(getTableRow().getItem()));
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttons);
                }
            });
        }

        private void setupFilters() {
            typeFilter.setItems(FXCollections.observableArrayList("Semua", "Pemasukan", "Pengeluaran"));
            typeFilter.setValue("Semua");

            // Load kategori dari database
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT category FROM transactions")) {
                
                ObservableList<String> categories = FXCollections.observableArrayList("Semua");
                while (rs.next()) {
                    categories.add(rs.getString("category"));
                }
                categoryFilter.setItems(categories);
                categoryFilter.setValue("Semua");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @FXML
        private void handleAddIncome() {
            showTransactionDialog("Pemasukan", true);
        }

        @FXML
        private void handleAddExpense() {
            showTransactionDialog("Pengeluaran", false);
        }

        private void showTransactionDialog(String title, boolean isIncome) {
            Dialog<Transaction> dialog = new Dialog<>();
            dialog.setTitle(title);

            ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField amount = new TextField();
            TextField category = new TextField();
            TextField description = new TextField();
            DatePicker date = new DatePicker(LocalDate.now());

            grid.add(new Label("Jumlah:"), 0, 0);
            grid.add(amount, 1, 0);
            grid.add(new Label("Kategori:"), 0, 1);
            grid.add(category, 1, 1);
            grid.add(new Label("Deskripsi:"), 0, 2);
            grid.add(description, 1, 2);
            grid.add(new Label("Tanggal:"), 0, 3);
            grid.add(date, 1, 3);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        double amt = Double.parseDouble(amount.getText());
                        if (!isIncome) amt = -amt;
                        return new Transaction(amt, category.getText(), description.getText(), 
                                            isIncome ? "INCOME" : "EXPENSE", date.getValue().toString());
                    } catch (NumberFormatException e) {
                        showAlert("Error", "Jumlah harus berupa angka");
                        return null;
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(transaction -> {
                saveTransaction(transaction);
                updateAll();
            });
        }

        private void saveTransaction(Transaction transaction) {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO transactions (user_id, amount, category, description, type, date) VALUES (?, ?, ?, ?, ?, ?)")) {
                
                pstmt.setLong(1, currentUser.getId());
                pstmt.setDouble(2, transaction.getAmount());
                pstmt.setString(3, transaction.getCategory());
                pstmt.setString(4, transaction.getDescription());
                pstmt.setString(5, transaction.getType());
                pstmt.setString(6, transaction.getDate());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Gagal menyimpan transaksi");
            }
        }

        private void handleEdit(Transaction transaction) {
            if (transaction == null) return;
            
            LocalDate transDate = LocalDate.parse(transaction.getDate());
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            
            if (transDate.isBefore(thirtyDaysAgo)) {
                showAlert("Error", "Transaksi lebih dari 30 hari tidak dapat diedit");
                return;
            }

            showTransactionDialog("Edit " + (transaction.getAmount() >= 0 ? "Pemasukan" : "Pengeluaran"), 
                                transaction.getAmount() >= 0);
        }

        private void handleDelete(Transaction transaction) {
            if (transaction == null) return;

            LocalDate transDate = LocalDate.parse(transaction.getDate());
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            
            if (transDate.isBefore(thirtyDaysAgo)) {
                showAlert("Error", "Transaksi lebih dari 30 hari tidak dapat dihapus");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Hapus");
            alert.setContentText("Apakah Anda yakin ingin menghapus transaksi ini?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
                        
                        pstmt.setLong(1, transaction.getId());
                        pstmt.executeUpdate();
                        updateAll();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert("Error", "Gagal menghapus transaksi");
                    }
                }
            });
        }

        @FXML
        private void handleSetTarget() {
            TextInputDialog dialog = new TextInputDialog(String.valueOf(spendingTarget));
            dialog.setTitle("Atur Target Pengeluaran");
            dialog.setHeaderText("Masukkan target pengeluaran bulanan:");
            dialog.setContentText("Target (Rp):");

            dialog.showAndWait().ifPresent(value -> {
                try {
                    spendingTarget = Double.parseDouble(value);
                    updateTargetProgress();
                } catch (NumberFormatException e) {
                    showAlert("Error", "Target harus berupa angka");
                }
            });
        }

        @FXML
        private void handleFilter() {
            updateTransactions();
            updateCharts();
        }

        private void updateAll() {
            updateBalance();
            updateTransactions();
            updateCharts();
            updateTargetProgress();
        }

        private void updateBalance() {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT SUM(amount) as balance FROM transactions WHERE user_id = ?")) {
                
                pstmt.setLong(1, currentUser.getId());
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    balanceLabel.setText(String.format("Rp %.2f", balance));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void updateTransactions() {
            try (Connection conn = DatabaseManager.getConnection()) {
                StringBuilder sql = new StringBuilder(
                    "SELECT * FROM transactions WHERE user_id = ?");
                
                if (startDate.getValue() != null)
                    sql.append(" AND date >= ?");
                if (endDate.getValue() != null)
                    sql.append(" AND date <= ?");
                if (!"Semua".equals(categoryFilter.getValue()))
                    sql.append(" AND category = ?");
                if (!"Semua".equals(typeFilter.getValue()))
                    sql.append(" AND type = ?");
                
                sql.append(" ORDER BY date DESC");
                
                PreparedStatement pstmt = conn.prepareStatement(sql.toString());
                int paramIndex = 1;
                pstmt.setLong(paramIndex++, currentUser.getId());
                
                if (startDate.getValue() != null)
                    pstmt.setString(paramIndex++, startDate.getValue().toString());
                if (endDate.getValue() != null)
                    pstmt.setString(paramIndex++, endDate.getValue().toString());
                if (!"Semua".equals(categoryFilter.getValue()))
                    pstmt.setString(paramIndex++, categoryFilter.getValue());
                if (!"Semua".equals(typeFilter.getValue()))
                    pstmt.setString(paramIndex++, typeFilter.getValue());
                
                ResultSet rs = pstmt.executeQuery();
                
                ObservableList<Transaction> transactions = FXCollections.observableArrayList();
                while (rs.next()) {
                    Transaction t = new Transaction(
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getString("date")
                    );
                    t.setId(rs.getLong("id"));
                    t.setUserId(rs.getLong("user_id"));
                    transactions.add(t);
                }
                
                transactionTable.setItems(transactions);
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void updateCharts() {
            // Update pie chart
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT category, SUM(amount) as total FROM transactions " +
                    "WHERE user_id = ? AND type = 'EXPENSE' " +
                    "GROUP BY category")) {
                
                pstmt.setLong(1, currentUser.getId());
                ResultSet rs = pstmt.executeQuery();
                
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                while (rs.next()) {
                    pieChartData.add(new PieChart.Data(
                        rs.getString("category"),
                        Math.abs(rs.getDouble("total"))
                    ));
                }
                categoryPieChart.setData(pieChartData);
                
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Update line chart
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT date, SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income, " +
                    "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense " +
                    "FROM transactions WHERE user_id = ? " +
                    "GROUP BY date ORDER BY date")) {
                
                pstmt.setLong(1, currentUser.getId());
                ResultSet rs = pstmt.executeQuery();
                
                XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
                XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
                incomeSeries.setName("Pemasukan");
                expenseSeries.setName("Pengeluaran");
                
                while (rs.next()) {
                    String date = rs.getString("date");
                    incomeSeries.getData().add(new XYChart.Data<>(date, rs.getDouble("income")));
                    expenseSeries.getData().add(new XYChart.Data<>(date, Math.abs(rs.getDouble("expense"))));
                }
                
                trendLineChart.getData().clear();
                trendLineChart.getData().addAll(incomeSeries, expenseSeries);
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void updateTargetProgress() {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT SUM(amount) as total_expense FROM transactions " +
                    "WHERE user_id = ? AND type = 'EXPENSE' AND date >= date('now', 'start of month')")) {
                
                pstmt.setLong(1, currentUser.getId());
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    double totalExpense = Math.abs(rs.getDouble("total_expense"));
                    double progress = totalExpense / spendingTarget;
                    targetProgress.setProgress(Math.min(progress, 1.0));
                    
                    String color = progress >= 1.0 ? "red" : (progress >= 0.8 ? "orange" : "green");
                    targetProgress.setStyle("-fx-accent: " + color);
                    
                    targetLabel.setText(String.format("Target: Rp %.2f / Rp %.2f", totalExpense, spendingTarget));
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void showAlert(String title, String content) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }
}
