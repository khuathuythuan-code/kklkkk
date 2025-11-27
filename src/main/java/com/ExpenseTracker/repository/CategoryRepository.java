package com.ExpenseTracker.repository;

import com.ExpenseTracker.model.Category;
import com.ExpenseTracker.utility.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryRepository {
    public List<String> findCategories(int userId, String type) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name, type FROM categories WHERE user_id=? AND type=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, userId);
            p.setString(2, type);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
//                    String typeDb = rs.getString("type");
                    list.add(name);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addCategory(int userId,String name, String type) {
        String sql = "INSERT INTO categories (user_id, name, type) VALUES (?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, userId);
            p.setString(2, name);
            p.setString(3, type);
            p.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCategory(int userId,String name) {
        String sql = "DELETE FROM categories WHERE user_id=? AND name=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, userId);
            p.setString(2, name);
            p.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
