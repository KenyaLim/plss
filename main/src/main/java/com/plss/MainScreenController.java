package com.plss;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import javafx.geometry.Insets;

public class MainScreenController {
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

    @FXML
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
        updatePieChart();
        updateLineChart();
    }

    private void updatePieChart() {
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
    }

    private void updateLineChart() {
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
