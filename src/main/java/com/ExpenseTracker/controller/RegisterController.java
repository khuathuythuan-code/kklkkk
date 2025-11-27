package com.ExpenseTracker.controller;

import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.model.User;
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
import java.time.LocalDate;

public class RegisterController {
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private Label titleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label passwordLabel;
    @FXML private Label confirmPassLabel;

    @FXML private Button signupBtn;
    @FXML private Hyperlink backToLoginLink;

    private UserRepository userRepository = new UserRepository();


    @FXML
    private void initialize(){
        // Cập nhật locale
        LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage);

        // Cập nhật text cho UI
        bindTexts();
    }


    @FXML
    private void handleRegister() throws IOException {
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String phoneStr = phoneField.getText().trim();

        // Validate form
        if (!ValidatorUlti.isEmailValid(email)) {
            showAlert(LanguageManagerUlti.get("signup.alert.invalid.email"));
            return;
        }
        if (!ValidatorUlti.isUsernameValid(username)) {
            showAlert(LanguageManagerUlti.get("signup.alert.invalid.username"));
            return;
        }
        if (userRepository.isUsernameExists(username)) {
            showAlert(LanguageManagerUlti.get("signup.alert.username.exists"));
            return;
        }
        if (userRepository.isEmailExists(email)) {
            showAlert(LanguageManagerUlti.get("signup.alert.email.exists"));
            return;
        }

        String passwordCheck = ValidatorUlti.isPasswordValid(password);
        if (passwordCheck.equals("lengthError")) {
            showAlert(LanguageManagerUlti.get("signup.alert.password.length.error"));
            return;
        } else if (passwordCheck.equals("typoError")) {
            showAlert(LanguageManagerUlti.get("signup.alert.password.typo.error"));
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(LanguageManagerUlti.get("signup.alert.password.notmatch"));
            return;
        }
        if (!ValidatorUlti.isPhoneValid(phoneStr)) {
            showAlert(LanguageManagerUlti.get("signup.alert.invalid.phone"));
            return;
        }

        int phone = Integer.parseInt(phoneStr);
        User user = new User(0, username, password, email, phone);

        if (userRepository.save(user)) {
            showAlert(LanguageManagerUlti.get("signup.alert.success"));
            changeScene();
        } else {
            showAlert(LanguageManagerUlti.get("signup.alert.fail"));
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LanguageManagerUlti.get("signup.alert.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void changeScene() throws IOException {
        Stage stage = (Stage) passwordField.getScene().getWindow();
        ChangeSceneUtil.navigate(stage, "/fxml/login.fxml");
    }

    private void bindTexts() {
        titleLabel.setText(LanguageManagerUlti.get("signup.title")); // nếu bạn muốn thêm

        usernameLabel.setText(LanguageManagerUlti.get("signup.username"));
        phoneLabel.setText(LanguageManagerUlti.get("signup.phone"));
        emailLabel.setText(LanguageManagerUlti.get("signup.email"));
        passwordLabel.setText(LanguageManagerUlti.get("signup.password"));
        confirmPassLabel.setText(LanguageManagerUlti.get("signup.passwordcheking"));

        signupBtn.setText(LanguageManagerUlti.get("signup.button.signup"));
        backToLoginLink.setText(LanguageManagerUlti.get("signup.hyperlink.backtologin"));

        usernameField.setPromptText(LanguageManagerUlti.get("signup.textfield.username.input"));
        phoneField.setPromptText(LanguageManagerUlti.get("signup.textfield.phone.input"));
        emailField.setPromptText(LanguageManagerUlti.get("signup.textfield.email.input"));
        passwordField.setPromptText(LanguageManagerUlti.get("signup.textfield.password.input"));
        confirmPasswordField.setPromptText(LanguageManagerUlti.get("signup.textfield.confirm.password.input"));

    }

}
