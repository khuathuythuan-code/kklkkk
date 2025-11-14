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

public class App extends Application {

    private Stage stage;

    // 🔹 Danh sách scene — đã thêm main-settings
    private final List<String> scenes = List.of(
            "login",
            "register",
            "main-income",
            "main-expense",
            "transaction",
            "settings",
            "savings-popup"
    );

    private int currentIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Expense Tracker - UI Test");
        loadScene(scenes.get(currentIndex));
        stage.show();
    }

    private void loadScene(String fxml) {
        try {
            System.out.println("🔹 Loading scene: " + fxml);

            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
            Parent root = loader.load();

            // 🔘 Hai nút điều hướng qua lại giữa các scene
            Button nextButton = new Button("Next ▶");
            Button backButton = new Button("◀ Back");

            // 💅 Style cho nút
            String navButtonStyle = """
                    -fx-background-color: #333;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-font-size: 13;
                    -fx-background-radius: 10;
                    -fx-padding: 8 16;
                    -fx-cursor: hand;
            """;
            nextButton.setStyle(navButtonStyle);
            backButton.setStyle(navButtonStyle);

            nextButton.setOnAction(e -> switchScene(1));
            backButton.setOnAction(e -> switchScene(-1));

            // ✅ StackPane chứa nội dung và nút điều hướng
            StackPane overlay = new StackPane(root);
            StackPane.setAlignment(nextButton, Pos.BOTTOM_RIGHT);
            StackPane.setAlignment(backButton, Pos.BOTTOM_LEFT);
            overlay.getChildren().addAll(nextButton, backButton);

            // 🎨 Nền đồng bộ dark mode
            overlay.setStyle("-fx-background-color: #121212;");

            Scene scene = new Scene(overlay, 1000, 600);
            stage.setScene(scene);

        } catch (IOException e) {
            System.err.println("❌ Không thể load file: " + fxml + ".fxml");
            e.printStackTrace();
        }
    }

    private void switchScene(int direction) {
        currentIndex = (currentIndex + direction + scenes.size()) % scenes.size();
        String nextScene = scenes.get(currentIndex);
        System.out.println("➡️ Switched to scene: " + nextScene);
        loadScene(nextScene);
    }

    public static void main(String[] args) {
        launch();
    }
}
