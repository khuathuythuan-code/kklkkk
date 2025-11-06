package org.example.project2ver2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.project2ver2.DBUtil;

import java.sql.*;

public class SettingsController {

    @FXML private ComboBox<String> themeCombo;
    @FXML private ComboBox<String> langCombo;
    @FXML private TextField currencyField;
    @FXML private TextField limitField;
    @FXML private Button saveBtn;
    @FXML private Button closeBtn;

    private final int currentUserId = 1;

    @FXML
    public void initialize() {
        themeCombo.getItems().addAll("light", "dark");
        langCombo.getItems().addAll("vi", "en");
        loadSettings();
    }

    private void loadSettings() {
        Connection c = DBUtil.getConnection();
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = c.prepareStatement("SELECT theme, language, currency, limit_amount FROM settings WHERE user_id=?");
            p.setInt(1, currentUserId);
            rs = p.executeQuery();
            if (rs.next()) {
                themeCombo.setValue(rs.getString("theme"));
                langCombo.setValue(rs.getString("language"));
                currencyField.setText(rs.getString("currency"));
                limitField.setText(String.valueOf(rs.getDouble("limit_amount")));
            } else {
                // default
                themeCombo.setValue("light");
                langCombo.setValue("vi");
                currencyField.setText("VND");
                limitField.setText("0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(c, p, rs);
        }
    }

    @FXML
    private void saveSettings() {
        String theme = themeCombo.getValue();
        String lang = langCombo.getValue();
        String currency = currencyField.getText();
        double limit = 0;
        try {
            limit = Double.parseDouble(limitField.getText());
        } catch (Exception ignored) {}

        Connection c = DBUtil.getConnection();
        PreparedStatement p = null;
        try {
            // try update first
            p = c.prepareStatement("UPDATE settings SET theme=?, language=?, currency=?, limit_amount=? WHERE user_id=?");
            p.setString(1, theme);
            p.setString(2, lang);
            p.setString(3, currency);
            p.setDouble(4, limit);
            p.setInt(5, currentUserId);
            int updated = p.executeUpdate();
            if (updated == 0) {
                DBUtil.closeAll(null, p, null);
                p = c.prepareStatement("INSERT INTO settings (user_id, theme, language, currency, limit_amount) VALUES (?,?,?,?,?)");
                p.setInt(1, currentUserId);
                p.setString(2, theme);
                p.setString(3, lang);
                p.setString(4, currency);
                p.setDouble(5, limit);
                p.executeUpdate();
            }
            new Alert(Alert.AlertType.INFORMATION, "Lưu cài đặt thành công").showAndWait();
        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi lưu cài đặt").showAndWait();
        } finally {
            DBUtil.closeAll(c, p, null);
        }
    }

    @FXML
    private void closeWindow() {
        Stage s = (Stage) closeBtn.getScene().getWindow();
        s.close();
    }

}
