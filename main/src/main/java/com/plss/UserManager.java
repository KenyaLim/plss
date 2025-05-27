package com.plss;

import java.sql.*;

public class UserManager {
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
