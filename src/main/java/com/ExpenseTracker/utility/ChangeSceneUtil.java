package com.ExpenseTracker.utility;

import com.ExpenseTracker.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ChangeSceneUtil {

    private static final String THEME = Singleton.getInstance().currentTheme; // đường dẫn CSS global

//    public static void navigate(Stage stage, String fxml) throws IOException {
//        Parent root = FXMLLoader.load(Objects.requireNonNull(ChangeSceneUtil.class.getResource(fxml)));
//
//        Scene scene = new Scene(root);
//
//        // Áp dụng theme hiện tại
//        ThemeUtil.applyTheme(scene);
//
//        stage.setScene(scene);
//        stage.show();
//    }


    // Hàm load FXML + apply theme (dùng chung)
    public static Parent loadFXML(String fxml) throws IOException {
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(ChangeSceneUtil.class.getResource(fxml))
        );
        return root;
    }

    // Điều hướng đổi toàn bộ scene của stage hiện tại
    public static void navigate(Stage stage, String fxml) throws IOException {
        Parent root = loadFXML(fxml);
        Scene scene = new Scene(root);
        ThemeUtil.applyTheme(scene);
        stage.setScene(scene);
        stage.show();
    }

}
