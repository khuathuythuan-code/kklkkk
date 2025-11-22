package com.ExpenseTracker.controller;

import com.ExpenseTracker.GoalMonitor;
import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.model.Goal;
import com.ExpenseTracker.model.Transaction;
import com.ExpenseTracker.repository.GoalRepository;
import com.ExpenseTracker.repository.TransactionRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class SetGoalPopupController {

    @FXML private TextField goalNameField, goalAmountField, spendingLimitField;
    @FXML private DatePicker goalDatePicker;
    @FXML private Slider spendingLimitSlider;
    @FXML private ProgressBar spendingProgressBar;
    @FXML private Label spendingLabel;
    @FXML private Button saveButton, editButton, deleteButton;
    @FXML private VBox currentGoalBox;
    @FXML private Label goalNameLabel, progressLabel, targetDateLabel, statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private ListView<String> completedGoalsList;
    @FXML private Button completeButton;
    @FXML private Label completedInfoLabel;

    private GoalRepository goalRepo;
    private TransactionRepository transactionRepo;
    private int currentUserId = Singleton.getInstance().currentUser ;
    private Goal currentGoal;


    public void initialize() {
        goalRepo = new GoalRepository();
        transactionRepo = new TransactionRepository();

        // Slider đồng bộ với text field
        spendingLimitSlider.valueProperty().addListener(
                (obs, ov, nv) -> spendingLimitField.setText(String.valueOf(nv.intValue()))
        );

        editButton.setOnAction(e -> {
            if (currentGoal != null) loadGoalForEdit(currentGoal);
        });

        deleteButton.setOnAction(e -> {
            if (currentGoal != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Bạn có chắc chắn muốn xóa mục tiêu: " + currentGoal.getGoalName() + "?",
                        ButtonType.OK, ButtonType.CANCEL);
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) deleteGoal(currentGoal);
                });
            }
        });

        loadGoals();
    }

    /** -----------------------
     * LOAD GOALS
     * ----------------------- */
    private void loadGoals() {
        try {
            List<Goal> goals = goalRepo.getGoalsByUserCached(currentUserId);

            // Mục tiêu chưa hoàn thành gần nhất
            currentGoal = goals.stream()
                    .filter(g -> !g.isCompleted())
                    .findFirst()
                    .orElse(null);

            showCurrentGoal(currentGoal);
            refreshProgress(); // Update progress bar ngay
            displayCompletedGoals(goals);

        } catch (SQLException e) {
            statusLabel.setText("Lỗi load mục tiêu: " + e.getMessage());
        }
    }

    private void showCurrentGoal(Goal goal) {
        if (goal == null) {
            currentGoalBox.setVisible(false);
            saveButton.setDisable(false);
            return;
        }

        currentGoalBox.setVisible(true);
        saveButton.setDisable(true);

        goalNameLabel.setText(goal.getGoalName());
        targetDateLabel.setText("Thời hạn: " + goal.getTargetDate());

        if (goal.isCompleted()) {
            editButton.setVisible(false);
            deleteButton.setVisible(false);
            completeButton.setVisible(false);
            completedInfoLabel.setText("Bắt đầu: " + goal.getCreatedDate().toLocalDate()
                    + " - Hoàn thành: " + goal.getCompletedDate());
            completedInfoLabel.setVisible(true);
        } else {
            editButton.setVisible(true);
            deleteButton.setVisible(true);
            completedInfoLabel.setVisible(false);
        }
    }

    /** -----------------------
     * REFRESH PROGRESS BAR
     * ----------------------- */
    private void refreshProgress() {
        if (currentGoal == null) return;

        long progress = calculateProgress(currentGoal);
        double progressRatio = Math.min((double) progress / currentGoal.getGoalAmount(), 1.0);
        Singleton.getInstance().goalSupervisorBar.set(progressRatio);
        progressBar.setProgress(progressRatio);
        progressLabel.setText(progress + " / " + currentGoal.getGoalAmount() + " VND");

        long spent = calculateSpending();
        double spentProgress = (double) spent / currentGoal.getSpendingLimit();
        spendingProgressBar.setProgress(Math.min(spentProgress , 1.0));
        spendingLabel.setText(spent + " / " + currentGoal.getSpendingLimit() + " VND");



        completeButton.setVisible(progress >= currentGoal.getGoalAmount());
        completeButton.setOnAction(e -> completeCurrentGoal(currentGoal));
    }




    /** -----------------------
     * CALCULATE GOAL PROGRESS (từ completedDate gần nhất)
     * ----------------------- */
    private long calculateProgress(Goal goal) {
        if (goal == null) return 0;

        final LocalDateTime finalStartDateTime = goal.getCreatedDate();

        // Tính progress từ các giao dịch
        double progressDouble = transactionRepo.findAllCached(currentUserId).stream()
                .filter(t -> t.getCreatedAt() != null)
                .filter(t -> !t.getCreatedAt().isBefore(finalStartDateTime))
                .mapToDouble(Transaction::getAmount)
                .sum();

        return (long) progressDouble;
    }

    /** -----------------------
     * CALCULATE SPENDING (từ completedDate gần nhất)
     * ----------------------- */
    private Long calculateSpending() {
        if (currentGoal == null) return (long) 0;
        final LocalDateTime finalStartDateTime = currentGoal.getCreatedDate();

        LocalDate now = LocalDate.now();
        return transactionRepo.findAllCached(currentUserId).stream()
                .filter(t -> "chi".equalsIgnoreCase(t.getType()))
                .filter(t -> t.getCreatedAt() != null)
                .filter(t -> !t.getCreatedAt().isBefore(finalStartDateTime))
                .mapToLong(t -> (long) -t.getAmount())
                .sum();

    }

    /** -----------------------
     * SAVE GOAL
     * ----------------------- */
    @FXML
    private void saveGoal() {
        try {
            String name = goalNameField.getText().trim();
            String amountStr = goalAmountField.getText().trim();
            String limitStr = spendingLimitField.getText().trim();
            LocalDate targetDate = goalDatePicker.getValue();
            LocalDate today = LocalDate.now();

            // VALIDATE
            if (name.isEmpty()) { statusLabel.setText("Tên mục tiêu không được để trống!"); goalNameField.requestFocus(); return; }
            if (amountStr.isEmpty()) { statusLabel.setText("Số tiền mục tiêu không được để trống!"); goalAmountField.requestFocus(); return; }
            if (limitStr.isEmpty()) { statusLabel.setText("Giới hạn chi tiêu không được để trống!"); spendingLimitField.requestFocus(); return; }
            if (targetDate == null) { statusLabel.setText("Vui lòng chọn thời hạn!"); goalDatePicker.requestFocus(); return; }

            long amount, limit;
            try { amount = Long.parseLong(amountStr); if (amount <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException e) { statusLabel.setText("Số tiền mục tiêu phải >0!"); goalAmountField.requestFocus(); return; }
            try { limit = Long.parseLong(limitStr); if (limit <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException e) { statusLabel.setText("Giới hạn chi tiêu phải >0!"); spendingLimitField.requestFocus(); return; }

            if (targetDate.isBefore(today)) {
                statusLabel.setText("Ngày kết thúc mục tiêu phải từ hôm nay trở đi!");
                goalDatePicker.requestFocus();
                return;
            }

            // Ngày bắt đầu dựa vào completedDate trước
            Goal lastCompletedGoal = goalRepo.getGoalsByUserCached(currentUserId).stream()
                    .filter(Goal::isCompleted)
                    .filter(g -> g.getCompletedDate() != null)
                    .max((g1, g2) -> g1.getCompletedDate().compareTo(g2.getCompletedDate()))
                    .orElse(null);

            LocalDate startDate = (lastCompletedGoal != null) ? lastCompletedGoal.getCompletedDate().toLocalDate().plusDays(1) : today;
            LocalDateTime goalCreatedDate = (today.isAfter(startDate) ? today.atStartOfDay() : startDate.atStartOfDay());

            // SAVE / UPDATE
            if (currentGoal == null) {
                Goal g = new Goal(0, currentUserId, name, amount, limit,
                        goalCreatedDate, targetDate, 0, 0, null);
                goalRepo.addGoal(g);
                statusLabel.setText("Đã tạo mục tiêu mới!");
                currentGoal = g;
            } else {
                currentGoal.setGoalName(name);
                currentGoal.setGoalAmount(amount);
                currentGoal.setSpendingLimit(limit);
                currentGoal.setTargetDate(targetDate);
                currentGoal.setCreatedDate(goalCreatedDate);
                goalRepo.updateGoal(currentGoal);
                statusLabel.setText("Đã cập nhật mục tiêu!");
            }

            clearForm();
            refreshProgress();
            loadGoals();
            GoalMonitor.resetPopups();


        } catch (SQLException e) { statusLabel.setText("Lỗi khi lưu mục tiêu: " + e.getMessage()); }
        catch (Exception e) { statusLabel.setText("Lỗi: " + e.getMessage()); }
    }

    private void clearForm() {
        goalNameField.clear();
        goalAmountField.clear();
        spendingLimitField.clear();
        goalDatePicker.setValue(null);
    }

    private void displayCompletedGoals(List<Goal> goals) {
        completedGoalsList.getItems().clear();
        for (Goal g : goals) {
            if (g.isCompleted()) {
                String text = g.getGoalName() + " - " + g.getGoalAmount() + " VND"
                        + " (Bắt đầu: " + g.getCreatedDate().toLocalDate()
                        + ", Hoàn thành: " + g.getCompletedDate() + ")";
                completedGoalsList.getItems().add(text);
            }
        }
    }

    private void completeCurrentGoal(Goal goal) {
        // đóng tất cả alert đang mở
        GoalMonitor.closeAllAlerts();

        try {
            goal.setIsCompleted(1);
            goal.setCompletedDate(LocalDateTime.now());
            goalRepo.updateGoal(goal);
            statusLabel.setText("Đã hoàn thành mục tiêu!");
            currentGoal = null;
            currentGoalBox.setVisible(false);
            saveButton.setDisable(false);
            loadGoals();
        } catch (SQLException e) {
            statusLabel.setText("Lỗi khi hoàn thành: " + e.getMessage());
        }
    }

    private void deleteGoal(Goal goal) {
        try {
            goalRepo.deleteGoal(goal.getId());
            statusLabel.setText("Đã xóa mục tiêu!");
            currentGoal = null;
            currentGoalBox.setVisible(false);
            saveButton.setDisable(false);
            loadGoals();
        } catch (SQLException e) {
            statusLabel.setText("Lỗi xóa: " + e.getMessage());
        }
    }

    private void loadGoalForEdit(Goal goal) {
        goalNameField.setText(goal.getGoalName());
        goalAmountField.setText(String.valueOf(goal.getGoalAmount()));
        spendingLimitField.setText(String.valueOf(goal.getSpendingLimit()));
        spendingLimitSlider.setValue(goal.getSpendingLimit());
        goalDatePicker.setValue(goal.getTargetDate());
        saveButton.setDisable(false);
        currentGoal = goal;
    }



}
