package org.example.logindemo2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label messageLabel;

    public void onRegister(ActionEvent e) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String confirm = confirmField.getText();

        if (!pass.equals(confirm)) {
            messageLabel.setText("❌ Passwords do not match!");
            return;
        }

        if (AuthService.register(user, pass)) {
            messageLabel.setText("✅ Account created!");
        } else {
            messageLabel.setText("⚠️ Username may already exist!");
        }
    }

    public void switchToLogin(ActionEvent e) {
        SceneSwitcher.switchScene(e, "login-view.fxml");
    }
}
