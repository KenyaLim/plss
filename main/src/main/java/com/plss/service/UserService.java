package com.plss.service;

import com.plss.model.User;
import com.plss.util.DatabaseUtil;

import java.sql.*;

public class UserService {
    public static User login(String phone, String passkey) {
        String query = "SELECT * FROM users WHERE phone = ? AND passkey = ?";
        
        try (PreparedStatement stmt = DatabaseUtil.getConnection().prepareStatement(query)) {
            stmt.setString(1, phone);
            stmt.setString(2, passkey);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(rs.getString("phone"), rs.getString("passkey"));
                user.setId(rs.getLong("id"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean registerUser(String phone, String passkey) {
        String query = "INSERT INTO users (phone, passkey) VALUES (?, ?)";
        
        try (PreparedStatement stmt = DatabaseUtil.getConnection().prepareStatement(query)) {
            stmt.setString(1, phone);
            stmt.setString(2, passkey);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
