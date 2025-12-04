package com.ExpenseTracker.repository;

import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.model.User;
import com.ExpenseTracker.utility.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


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

    public User getUser(int id) {
        Connection conn = DBUtil.getConnection();
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id); // Nhận đúng tham số truyền vào

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUserName(rs.getString("username"));
                u.setPassWord(rs.getString("password"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getInt("phone"));
                u.setTheme(rs.getString("theme"));
                u.setLanguage(rs.getString("language"));
                return u;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Không tìm thấy user
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

    public boolean updateUI(String pathTheme, String lang) {
        String sql = "UPDATE users SET theme = ?, language = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pathTheme.substring(pathTheme.lastIndexOf("/") + 1, pathTheme.lastIndexOf(".")));
            stmt.setString(2, lang);
            stmt.setInt(3, currentUserID);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Nếu có ít nhất 1 dòng bị update => thành công

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Catch exception => thất bại
        }
    }

//    public boolean updatePassword(String newPass) {
//        String sql = "UPDATE users SET password = ? WHERE id = ?";
//        try (Connection conn = DBUtil.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setString(1, newPass);
//            stmt.setInt(2, currentUserID);
//
//            int rowsAffected = stmt.executeUpdate();
//            return rowsAffected > 0; // Nếu có ít nhất 1 dòng bị update => thành công
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false; // Catch exception => thất bại
//        }
//    }

    public boolean updatePassword(String newPass) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPass);
            stmt.setInt(2, currentUserID);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Nếu có ít nhất 1 dòng bị update => thành công

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Catch exception => thất bại
        }
    }

}
