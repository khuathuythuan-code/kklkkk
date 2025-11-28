package com.ExpenseTracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();  // load FXML ra root

            // Gắn CSS vào root thì chỉ áp dụng cho mỗi cái file fxml đấy thôi
            root.getStylesheets().add(getClass().getResource(Singleton.getInstance().currentTheme).toExternalForm());

            Scene scene = new Scene(root);
            // Gắn CSS vào scene thì sẽ áp dụng cho nhiều file fxml bao gồm overlay, popup...
//            scene.getStylesheets().add(getClass().getResource(Singleton.getInstance().currentTheme).toExternalForm());


            primaryStage.setScene(scene);
            primaryStage.setWidth(900);
            primaryStage.setHeight(900);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}