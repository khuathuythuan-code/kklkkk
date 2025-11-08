package org.example.logindemo2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    public void onLogin(ActionEvent e) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        if (AuthService.login(user, pass)) {
            messageLabel.setText("✅ Login successful!");
        } else {
            messageLabel.setText("❌ Invalid username or password");
        }
    }

    public void switchToRegister(ActionEvent e) {
        SceneSwitcher.switchScene(e, "register-view.fxml");
    }
}
