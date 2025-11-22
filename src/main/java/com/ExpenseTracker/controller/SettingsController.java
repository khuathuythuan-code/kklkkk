package com.ExpenseTracker.controller;

import com.ExpenseTracker.utility.ChangeSceneUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {

    @FXML
    private void changeScene(ActionEvent e) throws IOException {
        Button btn = (Button) e.getSource();
        String id = btn.getId();

        String fxml = switch (id) {
            case "btnHome" -> "/fxml/main-expense.fxml";
            case "btnHistory" -> "/fxml/history.fxml";
            case "btnReport" -> "/fxml/chart.fxml";
            case "btnSettings" -> "/fxml/settings.fxml";
            default -> throw new IllegalArgumentException("Không xác định nút: " + id);
        };

        Stage stage = (Stage) btn.getScene().getWindow();
        ChangeSceneUtil.navigate(stage, fxml);
    }
}
