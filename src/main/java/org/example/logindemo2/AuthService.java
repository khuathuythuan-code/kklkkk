package org.example.logindemo2;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class AuthService {

    public static boolean register(String username, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users(username, password) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hash);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Đăng ký thất bại: " + e.getMessage());
            return false;
        }
    }

    public static boolean login(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password");
                return BCrypt.checkpw(password, hash);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đăng nhập: " + e.getMessage());
        }
        return false;
    }
}
