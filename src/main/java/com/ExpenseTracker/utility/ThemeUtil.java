package com.ExpenseTracker.utility;

import com.ExpenseTracker.Singleton;
import javafx.scene.Scene;

public class ThemeUtil {
    public static void applyTheme(Scene scene) {
        Singleton s = Singleton.getInstance();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                ThemeUtil.class.getResource(s.currentTheme).toExternalForm()
        );
    }
}
