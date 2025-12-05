package com.ExpenseTracker;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Singleton {
    // 1. private static instance
    private static Singleton instance;
    public boolean isDarkMode = false;
    public String currentUserTheme;
    public String currentTheme = "/css/dark.css";
    // 2. public variable
    public int currentUser;

    public String currentLanguage = "en";
    // Goal progress global, mọi listener subscribe
    public DoubleProperty goalSupervisorBar = new SimpleDoubleProperty(0);

    // 3. private constructor để không thể tạo instance từ ngoài
    private Singleton() {}

    // 4. public method để lấy instance duy nhất
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }


    public void changeTheme() {
        currentTheme = !currentTheme.equalsIgnoreCase("/css/dark.css")
                ? "/css/dark.css"
                : "/css/light.css";

        isDarkMode = currentTheme.equalsIgnoreCase("/css/dark.css");
    }


    public void applyUserTheme(){
        currentTheme = currentUserTheme.equalsIgnoreCase("light")? "/css/light.css":"/css/dark.css";
        isDarkMode = currentTheme.equalsIgnoreCase("/css/dark.css");
    }

    public void setDefault(){
        currentTheme = "/css/dark.css";
        currentLanguage = "en";
    }

}
