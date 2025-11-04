package org.example.project2ver1;

import javafx.application.Application;

import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;

import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 450);
        stage.setTitle("Quản lý tài chính cá nhân");
        stage.setScene(scene);
        stage.show();
    }


    public static void setRoot(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
        stage.setScene(new Scene(loader.load()));
    }


    public static void main(String[] args) {
        launch();
    }
}