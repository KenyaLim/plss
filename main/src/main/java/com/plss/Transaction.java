package com.plss;

import javafx.beans.property.*;

public class Transaction {
    private long id;
    private long userId;
    private DoubleProperty amount;
    private StringProperty category;
    private StringProperty description;
    private StringProperty type; // "INCOME" or "EXPENSE"
    private StringProperty date;
    
    public Transaction(double amount, String category, String description, String type, String date) {
        this.amount = new SimpleDoubleProperty(amount);
        this.category = new SimpleStringProperty(category);
        this.description = new SimpleStringProperty(description);
        this.type = new SimpleStringProperty(type);
        this.date = new SimpleStringProperty(date);
    }
    
    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    
    public double getAmount() { return amount.get(); }
    public void setAmount(double value) { amount.set(value); }
    public DoubleProperty amountProperty() { return amount; }
    
    public String getCategory() { return category.get(); }
    public void setCategory(String value) { category.set(value); }
    public StringProperty categoryProperty() { return category; }
    
    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }
    
    public String getType() { return type.get(); }
    public void setType(String value) { type.set(value); }
    public StringProperty typeProperty() { return type; }
    
    public String getDate() { return date.get(); }
    public void setDate(String value) { date.set(value); }
    public StringProperty dateProperty() { return date; }
}
