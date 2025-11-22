package com.ExpenseTracker.controller;

import com.ExpenseTracker.model.User;
import com.ExpenseTracker.repository.UserRepository;
import com.ExpenseTracker.utility.ChangeSceneUtil;
import com.ExpenseTracker.utility.DBUtil;
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


    private UserRepository userRepository = new UserRepository();


    @FXML
    private void handleRegister() throws IOException {
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String phoneStr = phoneField.getText().trim();

        // Validate form
        if (!ValidatorUlti.isEmailValid(email)) { showAlert("Email không hợp lệ"); return; }
        if (!ValidatorUlti.isUsernameValid(username)) { showAlert("Tên đăng nhập không hợp lệ"); return; }
        if (userRepository.isUsernameExists(username)) { showAlert("Tên đăng nhập đã tồn tại"); return; }
        if (userRepository.isEmailExists(email)) { showAlert("Email đã tồn tại"); return; }
        if (!ValidatorUlti.isPasswordValid(password)) { showAlert("Mật khẩu tối thiểu 6 ký tự"); return; }
        if (!password.equals(confirmPassword)) { showAlert("Mật khẩu xác nhận không khớp"); return; }
        if (!ValidatorUlti.isPhoneValid(phoneStr)) { showAlert("Số điện thoại không hợp lệ"); return; }

        int phone = Integer.parseInt(phoneStr);

        User user = new User(0, username, password, email, phone);
        if (userRepository.save(user)) {
            showAlert("Đăng ký thành công!");
            changeScene();
        } else {
            showAlert("Đăng ký thất bại!");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void changeScene() throws IOException {
        Stage stage = (Stage) passwordField.getScene().getWindow();
        ChangeSceneUtil.navigate(stage, "/fxml/login.fxml");
    }
}
