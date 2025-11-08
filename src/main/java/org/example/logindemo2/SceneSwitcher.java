package org.example.logindemo2;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;

public class SceneSwitcher {
    public static void switchScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(SceneSwitcher.class.getResource(fxmlFile));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            FadeTransition ft = new FadeTransition(Duration.millis(400), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
