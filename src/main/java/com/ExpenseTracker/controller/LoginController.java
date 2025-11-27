package com.ExpenseTracker.controller;

import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.repository.UserRepository;
import com.ExpenseTracker.utility.ChangeSceneUtil;
import com.ExpenseTracker.utility.DBUtil;
import com.ExpenseTracker.utility.LanguageManagerUlti;
import com.ExpenseTracker.utility.ValidatorUlti;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class LoginController {


    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label title,userNameLabel,passwordLabel;

    @FXML private Button loginBtn,switchToRegisterBtn,languageBtn;

    private UserRepository userRepository = new UserRepository();

    @FXML
    private void initialize(){
        LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage); // default
        bindTexts();

        languageBtn.setOnAction(e -> {
            // Đổi ngôn ngữ
            Singleton.getInstance().currentLanguage =
                    Singleton.getInstance().currentLanguage.equals("vi") ? "en" : "vi";

            // Cập nhật locale
            LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage);

            // Cập nhật text cho UI
            bindTexts();
        });

    }

    @FXML
    private void onLogin(ActionEvent event) throws IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (!ValidatorUlti.isUsernameValid(username)) {
            showAlert(LanguageManagerUlti.get("login.alert.invalid.username"));
            return;
        }

        String check = ValidatorUlti.isPasswordValid(password);
        if (check.equals("lengthError")) {
            showAlert(LanguageManagerUlti.get("login.alert.password.length.error"));
            return;
        } else if (check.equals("typoError")) {
            showAlert(LanguageManagerUlti.get("login.alert.password.typo.error"));
            return;
        }

        if (userRepository.checkLogin(username, password)) {
            showAlert(LanguageManagerUlti.get("login.alert.success"));
            Singleton.getInstance().currentUser = UserRepository.currentUserID;
            changeScene(event);
        } else {
            showAlert(LanguageManagerUlti.get("login.alert.fail"));
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LanguageManagerUlti.get("login.alert.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void changeScene(ActionEvent e) throws IOException {
        Button btn = (Button) e.getSource();
        String id = btn.getId();

        String fxml = switch (id) {
            case "loginBtn" -> "/fxml/main-expense.fxml";
            case "switchToRegisterBtn" -> "/fxml/register.fxml";
            default -> throw new IllegalArgumentException("Không xác định nút: " + id);
        };

        Stage stage = (Stage) btn.getScene().getWindow();
        ChangeSceneUtil.navigate(stage, fxml);
    }


    private void bindTexts(){
        title.setText(LanguageManagerUlti.get("login.title"));
        userNameLabel.setText(LanguageManagerUlti.get("login.username"));
        passwordLabel.setText(LanguageManagerUlti.get("login.password"));
        loginBtn.setText(LanguageManagerUlti.get("login.button.login"));
        switchToRegisterBtn.setText(LanguageManagerUlti.get("login.button.signup"));
        usernameField.setPromptText(LanguageManagerUlti.get("login.textfield.username.input"));
        passwordField.setPromptText(LanguageManagerUlti.get("login.textfield.password.input"));
    }
}
