package projek_keuangan.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DatabaseConnection {
    private static final String DB_PATH = "data/keuangan.db";
    
    public static Connection getConnection() throws SQLException {
        File directory = new File("data");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL" +
                    ")");

            // Create transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "tanggal DATE," +
                    "nominal DECIMAL(15,2)," +
                    "catatan TEXT," +
                    "kategori TEXT," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
