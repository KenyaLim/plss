package com.plss.util;

import java.sql.*;

public class DatabaseUtil {
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
