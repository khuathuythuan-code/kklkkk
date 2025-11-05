package org.example.project2ver2.repository;

import org.example.project2ver2.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {
    public List<String> findAll(int userId){
        List<String> list = new ArrayList<>();
        Connection c = DBUtil.getConnection();
        try (PreparedStatement p = c.prepareStatement("SELECT name FROM categories WHERE user_id=?")) {
            p.setInt(1, userId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(rs.getString("name"));
            rs.close();
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void add(int userId, String name) {
        Connection c = DBUtil.getConnection();
        try (PreparedStatement p = c.prepareStatement("INSERT INTO categories (name,user_id) VALUES (?,?)")) {
            p.setString(1, name);
            p.setInt(2, userId);
            p.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteByName(int userId, String name) {
        Connection c = DBUtil.getConnection();
        try (PreparedStatement p = c.prepareStatement("DELETE FROM categories WHERE user_id=? AND name=?")) {
            p.setInt(1, userId); p.setString(2, name); p.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
