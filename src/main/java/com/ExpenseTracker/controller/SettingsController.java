package com.ExpenseTracker.controller;

import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.repository.UserRepository;
import com.ExpenseTracker.utility.ChangeSceneUtil;
import com.ExpenseTracker.utility.LanguageManagerUlti;
import com.ExpenseTracker.utility.ThemeUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {
    @FXML private Label titleLabel;

    @FXML private Label themeOptionLabel;
    @FXML private Label languageOptionLabel;
    @FXML private Label changePasswordLabel;

    @FXML private ComboBox<String> btnChangeLanguage;
    @FXML private Button btnChangePassword;

    @FXML private Button btnLogout;

    @FXML private Button btnHome;
    @FXML private Button btnHistory;
    @FXML private Button btnReport;
    @FXML private Button btnSettings;
    @FXML private ToggleButton themeToggle;
    private UserRepository userRepo = new UserRepository();

    @FXML
    private void initialize(){
        // Cập nhật locale
        LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage);

        // Cập nhật text cho UI
        bindTexts();

        btnChangeLanguage.getItems().addAll("Tiếng Việt", "English");
        btnChangeLanguage.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            Singleton.getInstance().currentLanguage =
                    newVal.equalsIgnoreCase("English") ? "en" : "vi";
            LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage);
            bindTexts();
            userRepo.updateUI(Singleton.getInstance().currentTheme, Singleton.getInstance().currentLanguage);
        });


        btnLogout.setOnAction(e -> {
            try {
                Singleton.getInstance().setDefault();
                Singleton.getInstance().currentUserTheme = null;
                changeScene(e);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        btnChangePassword.setOnAction(this::openDialog);
        themeToggle.setText(Singleton.getInstance().isDarkMode ? "🌙" : "🌞");


        themeToggle.setOnAction(e -> {
            Singleton.getInstance().changeTheme();     // đổi theme trong singleton
            ThemeUtil.applyTheme(themeToggle.getScene()); // cập nhật ngay Scene hiện tại
            // cập nhật icon
            themeToggle.setText(Singleton.getInstance().isDarkMode ? "🌙" : "🌞");
            userRepo.updateUI(Singleton.getInstance().currentTheme, Singleton.getInstance().currentLanguage);
        });
    }


    @FXML
    private void changeScene(ActionEvent e) throws IOException {
        Button btn = (Button) e.getSource();
        String id = btn.getId();

        String fxml = switch (id) {
            case "btnHome" -> "/fxml/main-expense.fxml";
            case "btnHistory" -> "/fxml/history.fxml";
            case "btnReport" -> "/fxml/chart.fxml";
            case "btnSettings" -> "/fxml/settings.fxml";
            case "btnLogout" -> "/fxml/login.fxml";
            default -> throw new IllegalArgumentException("Không xác định nút: " + id);
        };

        Stage stage = (Stage) btn.getScene().getWindow();
        ChangeSceneUtil.navigate(stage, fxml);
    }

    private void bindTexts() {

        // Title
        titleLabel.setText(LanguageManagerUlti.get("Settings.label.title"));

        // Options
        themeOptionLabel.setText(LanguageManagerUlti.get("Settings.label.theme.option"));
        languageOptionLabel.setText(LanguageManagerUlti.get("Settings.label.language.option"));
        changePasswordLabel.setText(LanguageManagerUlti.get("Settings.label.change.password"));

        // Combobox default text
        btnChangeLanguage.setPromptText(LanguageManagerUlti.get("Settings.combobox.language.select"));

        // Logout button
        btnLogout.setText(LanguageManagerUlti.get("Settings.button.logout"));

        // Bottom Menu
        btnHome.setText(LanguageManagerUlti.get("Settings.button.menu.home"));
        btnHistory.setText(LanguageManagerUlti.get("Settings.button.menu.history"));
        btnReport.setText(LanguageManagerUlti.get("Settings.button.menu.report"));
        btnSettings.setText(LanguageManagerUlti.get("Settings.button.menu.settings"));
    }

    private void openDialog(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/password-changing-popup.fxml"));
            Stage main = (Stage) btnChangePassword.getScene().getWindow();
            Stage st = new Stage();
            st.initOwner(main);
            st.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(loader.load());
            ThemeUtil.applyTheme(scene);   // áp dụng CSS vào popup
//            st.setScene(new Scene(loader.load()));
            st.setScene(scene);
            st.showAndWait();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
