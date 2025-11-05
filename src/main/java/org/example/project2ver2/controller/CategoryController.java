package org.example.project2ver2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.project2ver2.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryController {

    @FXML private ListView<String> categoryList;
    @FXML private TextField newCategoryField;
    @FXML private ComboBox<String> typeCombo;

    private final int currentUserId = 1;

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("Chi", "Thu");
        typeCombo.setValue("Chi");
        loadCategories();
    }

    private void loadCategories() {
        categoryList.getItems().clear();
        List<String> cats = findCategoriesFromDb();
        categoryList.getItems().addAll(cats);
    }

    private List<String> findCategoriesFromDb() {
        List<String> list = new ArrayList<>();
        Connection c = DBUtil.getConnection();
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = c.prepareStatement("SELECT name, type FROM categories WHERE user_id=?");
            p.setInt(1, currentUserId);
            rs = p.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type");
                list.add(name + " (" + type + ")");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DBUtil.closeAll(c, p, rs);
        }
        return list;
    }

    @FXML
    private void addCategory() {
        String name = newCategoryField.getText();
        String type = typeCombo.getValue();
        if (name == null || name.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Tên danh mục trống").showAndWait();
            return;
        }
        Connection c = DBUtil.getConnection();
        PreparedStatement p = null;
        try {
            p = c.prepareStatement("INSERT INTO categories (user_id, name, type) VALUES (?,?,?)");
            p.setInt(1, currentUserId);
            p.setString(2, name);
            p.setString(3, type);
            p.executeUpdate();
            newCategoryField.clear();
            loadCategories();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(c, p, null);
        }
    }

    @FXML
    private void deleteSelected() {
        String sel = categoryList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        // sel format: "Tên (Type)"
        String name = sel.replaceAll("\\s*\\(.*\\)$", "").trim();
        Connection c = DBUtil.getConnection();
        PreparedStatement p = null;
        try {
            p = c.prepareStatement("DELETE FROM categories WHERE user_id=? AND name=?");
            p.setInt(1, currentUserId);
            p.setString(2, name);
            p.executeUpdate();
            loadCategories();
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.closeAll(c, p, null); }
    }

    @FXML
    private void closeDialog() {
        Stage s = (Stage) categoryList.getScene().getWindow();
        s.close();
    }
}
