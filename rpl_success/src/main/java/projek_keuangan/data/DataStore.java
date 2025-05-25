package projek_keuangan.data;

import projek_keuangan.db.DatabaseConnection;
import projek_keuangan.item.keuanganItem;
import projek_keuangan.item.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    public static boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User findUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<keuanganItem> getTodos(String username) {
        List<keuanganItem> items = new ArrayList<>();
        String sql = "SELECT t.* FROM transactions t " +
                    "JOIN users u ON t.user_id = u.id " +
                    "WHERE u.username = ?";
                    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                items.add(new keuanganItem(
                    rs.getDate("tanggal").toString(),
                    String.valueOf(rs.getBigDecimal("nominal")),
                    rs.getString("catatan"),
                    rs.getString("kategori")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static void addTodo(String username, keuanganItem item) {
        String sql = "INSERT INTO transactions (user_id, tanggal, nominal, catatan, kategori) " +
                    "SELECT id, ?, ?, ?, ? FROM users WHERE username = ?";
                    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(item.getTanggal()));
            pstmt.setBigDecimal(2, new java.math.BigDecimal(item.getNominal()));
            pstmt.setString(3, item.getCatatan());
            pstmt.setString(4, item.getKategori());
            pstmt.setString(5, username);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeTodo(String username, keuanganItem item) {
        String sql = "DELETE t FROM transactions t " +
                    "JOIN users u ON t.user_id = u.id " +
                    "WHERE u.username = ? AND t.tanggal = ? AND t.catatan = ?";
                    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setDate(2, Date.valueOf(item.getTanggal()));
            pstmt.setString(3, item.getCatatan());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editTodo(String username, keuanganItem oldItem, keuanganItem newItem) {
        String sql = "UPDATE transactions SET tanggal = ?, nominal = ?, catatan = ?, kategori = ? " +
                    "WHERE user_id = (SELECT id FROM users WHERE username = ?) " +
                    "AND tanggal = ? AND catatan = ?";
                    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(newItem.getTanggal()));
            pstmt.setBigDecimal(2, new java.math.BigDecimal(newItem.getNominal().replaceAll("[^\\d.]", "")));
            pstmt.setString(3, newItem.getCatatan());
            pstmt.setString(4, newItem.getKategori());
            pstmt.setString(5, username);
            pstmt.setDate(6, Date.valueOf(oldItem.getTanggal()));
            pstmt.setString(7, oldItem.getCatatan());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}


