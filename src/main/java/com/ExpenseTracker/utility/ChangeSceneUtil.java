package com.ExpenseTracker.utility;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ChangeSceneUtil {
    public static void navigate(Stage stage, String fxml) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(ChangeSceneUtil.class.getResource(fxml)));
        stage.setScene(new Scene(root));
        stage.show();
    }
}
