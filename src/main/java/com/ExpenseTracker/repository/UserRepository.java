package com.ExpenseTracker.repository;

import com.ExpenseTracker.model.User;
import com.ExpenseTracker.utility.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;


public class UserRepository {
    public static int currentUserID;

    public boolean checkLogin(String username, String password) {
        Connection conn = DBUtil.getConnection();
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {  // di chuyển đến bản ghi đầu tiên
                currentUserID = rs.getInt("id");
                return true;  // tồn tại -> login thành công
            } else {
                return false; // không tồn tại
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Kiểm tra username tồn tại
    public boolean isUsernameExists(String username) {
        Connection conn = DBUtil.getConnection();
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Kiểm tra email tồn tại
    public boolean isEmailExists(String email) {
        Connection conn = DBUtil.getConnection();
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Thêm user mới
    public boolean save(User user) {
        Connection conn = DBUtil.getConnection();
        String sql = "INSERT INTO users(username, password, email, phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUserName());
            stmt.setString(2, user.getPassWord());
            stmt.setString(3, user.getEmail());
            stmt.setInt(4, user.getPhone());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
