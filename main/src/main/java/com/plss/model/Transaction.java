package com.plss.model;

public class Transaction {
    private long id;
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
