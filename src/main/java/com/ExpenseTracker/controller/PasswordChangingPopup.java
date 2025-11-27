package com.ExpenseTracker.controller;

import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.model.User;
import com.ExpenseTracker.repository.UserRepository;
import com.ExpenseTracker.utility.ChangeSceneUtil;
import com.ExpenseTracker.utility.LanguageManagerUlti;
import com.ExpenseTracker.utility.ValidatorUlti;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class PasswordChangingPopup {

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordLabel, titleLabel;
    @FXML private Label confirmPassLabel;
    @FXML private Button saveBtn;

    private UserRepository userRepository = new UserRepository();


    @FXML
    private void initialize(){
        // Cập nhật locale
        LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage);

        // Cập nhật text cho UI
        bindTexts();

        saveBtn.setOnAction(e-> {
            try {
                handleRegister();
                closeWindow();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }


    @FXML
    private void handleRegister() throws IOException {
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();


        // Validate form
        String check = ValidatorUlti.isPasswordValid(password);

        if (check.equals("lengthError")) {
            showAlert(LanguageManagerUlti.get("PasswordChangingPopup.notif.password_too_short"));
            return;
        } else if (check.equals("typoError")) {
            showAlert(LanguageManagerUlti.get("PasswordChangingPopup.notif.password_typo"));
            return;
        }


        if (!password.equals(confirmPassword)) { showAlert(LanguageManagerUlti.get("PasswordChangingPopup.notif.password_mismatch")); return; }


        if (userRepository.updatePassword(password)) {
            showAlert(LanguageManagerUlti.get("PasswordChangingPopup.notif.password_change_success"));
        } else {
            showAlert(LanguageManagerUlti.get("PasswordChangingPopup.notif.password_change_fail"));
        }
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LanguageManagerUlti.get("PasswordChangingPopup.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow(){
        Stage s = (Stage) saveBtn.getScene().getWindow();
        s.close();
    }


    private void bindTexts() {
        titleLabel.setText(LanguageManagerUlti.get("PasswordChangingPopup.title"));
        passwordLabel.setText(LanguageManagerUlti.get("PasswordChangingPopup.label.enterNewPass"));
        confirmPassLabel.setText(LanguageManagerUlti.get("PasswordChangingPopup.label.enterNewPassAgain"));
        saveBtn.setText(LanguageManagerUlti.get("PasswordChangingPopup.button.save"));
    }
}
