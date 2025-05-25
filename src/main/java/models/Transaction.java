package models;

import java.util.Date;

public class Transaction {
    private String id;
    private String userId;
    private double amount;
    private String category;
    private String description;
    private Date date;
    private boolean isIncome;

    // Constructor
    public Transaction(String id, String userId, double amount, String category, 
                      String description, Date date, boolean isIncome) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.isIncome = isIncome;
    }

    // Getters and setters
    // ... implement standard getters and setters ...
}
