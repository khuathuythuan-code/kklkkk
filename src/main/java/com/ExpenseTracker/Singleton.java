package com.ExpenseTracker;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Singleton {
    // 1. private static instance
    private static Singleton instance;

    // 2. public variable
    public int currentUser;

    public String currentLanguage = "vi";
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
}
