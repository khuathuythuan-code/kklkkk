package com.ExpenseTracker;

import com.ExpenseTracker.model.Goal;
import com.ExpenseTracker.model.Transaction;
import com.ExpenseTracker.repository.GoalRepository;
import com.ExpenseTracker.repository.TransactionRepository;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GoalMonitor {

    private static boolean completionPopupShown = false;
    private static boolean overspendPopupShown = false;
    private static boolean overduePopupShown = false;
    private static boolean setGoalFormOpened = false;

    private static GoalRepository goalRepo = new GoalRepository();
    private static TransactionRepository transRepo = new TransactionRepository();
    private static final List<Alert> openAlerts = new ArrayList<>();

    private static int userId;
    private static Timer timer;

    // ProgressBar và Label hiện tại (scene bất kỳ)
    private static ProgressBar currentBar;
    private static Label currentLabel;

    /** Bắt đầu GoalMonitor cho user hiện tại */
    public static void start(int currentUserId) {
        userId = currentUserId;

        if (timer != null) timer.cancel();
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkAndUpdate();
            }
        }, 0, 3000);
    }

    /** Đăng ký ProgressBar + Label hiện tại (scene mới) */
    public static void registerProgressBar(ProgressBar bar, Label label) {
        currentBar = bar;
        currentLabel = label;
        updateProgressBarNow(); // cập nhật ngay khi load scene
    }

    /** Cập nhật ngay ProgressBar + Label */
    public static void updateProgressBarNow() {
        Platform.runLater(() -> {
            Goal goal = null;
            try {
                goal = goalRepo.getGoalsByUserFromDB(userId).stream()
                        .filter(g -> !g.isCompleted())
                        .findFirst()
                        .orElse(null);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            double ratio = 0;
            if (goal != null) {
                long progress = calculateProgress(goal);
                ratio = clamp((double) progress / goal.getGoalAmount());
            }

            if (currentBar != null) currentBar.setProgress(ratio);
            if (currentLabel != null) currentLabel.setText(String.format("%.0f%%", ratio * 100));
        });
    }

    private static double clamp(double ratio) {
        if (ratio < 0) return 0;
        if (ratio > 1) return 1;
        return ratio;
    }

    /** Kiểm tra và cập nhật progress + popup */
    private static void checkAndUpdate() {
        Goal goal;
        try {
            goal = goalRepo.getGoalsByUserFromDB(userId).stream()
                    .filter(g -> !g.isCompleted())
                    .findFirst()
                    .orElse(null);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (goal == null) {
            updateProgressBarNow();
            return;
        }

        long progress = calculateProgress(goal);
        long spent = calculateSpending(goal);

        // Cập nhật ProgressBar
        updateProgressBarNow();

        // --- popup hoàn thành ---
        if (!completionPopupShown && progress >= goal.getGoalAmount()) {
            completionPopupShown = true;
            showPopup("Hoàn thành mục tiêu!",
                    "Chúc mừng! Bạn đã đạt mục tiêu tiết kiệm!\nNhấn hoàn thành để tắt thông báo",
                    true);
        }

        // --- popup vượt hạn mức ---
        if (!overspendPopupShown && spent > goal.getSpendingLimit()) {
            overspendPopupShown = true;
            showPopup("Vượt hạn mức!",
                    "Bạn đã chi tiêu vượt giới hạn!\nHãy nâng hạn mức để tắt cảnh báo",
                    true);
        }

        // --- popup quá hạn ---
        if (!overduePopupShown && goal.getTargetDate().isBefore(LocalDate.now()) && !goal.isCompleted()) {
            overduePopupShown = true;
            showPopup("Mục tiêu quá hạn!",
                    "Bạn đã không đạt được mục tiêu trước thời hạn!\nHãy đặt thời hạn thêm để tắt thông báo",
                    true);
        }
    }


    public static void resetPopups() {
        completionPopupShown = false;
        overspendPopupShown = false;
        overduePopupShown = false;
    }


    private static long calculateProgress(Goal goal) {
        LocalDateTime start = goal.getCreatedDate();
        return (long) transRepo.findAll(userId).stream()
                .filter(t -> t.getCreatedAt() != null)
                .filter(t -> !t.getCreatedAt().isBefore(start))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private static long calculateSpending(Goal goal) {
        LocalDateTime start = goal.getCreatedDate();
        return transRepo.findAll(userId).stream()
                .filter(t -> "chi".equalsIgnoreCase(t.getType()))
                .filter(t -> t.getCreatedAt() != null)
                .filter(t -> !t.getCreatedAt().isBefore(start))
                .mapToLong(t -> (long) -t.getAmount())
                .sum();
    }

    private static void showPopup(String title, String message, boolean openSetGoalOnOK) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(message);

            openAlerts.add(alert); // lưu lại popup

            alert.setOnHidden(e -> openAlerts.remove(alert)); // tự xóa khi đóng

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK && openSetGoalOnOK && !setGoalFormOpened) {
                    try {
                        FXMLLoader loader = new FXMLLoader(GoalMonitor.class.getResource("/fxml/set-goal-popup.fxml"));
                        Stage stage = new Stage();
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setScene(new Scene(loader.load()));
                        setGoalFormOpened = true;
                        stage.showAndWait();
                        setGoalFormOpened = false;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });
    }


    public static void closeAllAlerts() {
        Platform.runLater(() -> {
            for (Alert alert : new ArrayList<>(openAlerts)) {
                if (alert.isShowing()) {
                    alert.close();
                }
            }
            openAlerts.clear();
        });
    }



}
