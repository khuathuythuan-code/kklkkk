package com.ExpenseTracker.controller;

import com.ExpenseTracker.GoalMonitor;
import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.model.Goal;
import com.ExpenseTracker.model.Transaction;
import com.ExpenseTracker.repository.GoalRepository;
import com.ExpenseTracker.repository.TransactionRepository;
import com.ExpenseTracker.utility.LanguageManagerUlti;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @FXML private Label titleLabel;

    @FXML private Label goalLabel;
    @FXML private Label goalAmountLabel;
    @FXML private Label goalDateLabel;
    @FXML private Label spendingLimitLabel;
    @FXML private Label currentGoalDate;
    @FXML private Label progressTitle;
    @FXML private Label spendingTitle;
    @FXML private Label completedGoalsTitle;
    @FXML private Label progressTitleLabel;
    @FXML private Label spendingTitleLabel;


    private GoalRepository goalRepo;
    private TransactionRepository transactionRepo;
    private int currentUserId = Singleton.getInstance().currentUser ;
    private Goal currentGoal;


    public void initialize() {
        // Cập nhật locale
        LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage);

        // Cập nhật text cho UI
        bindTexts();
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
                        LanguageManagerUlti.get("SetGoalPopup.status.goal.delete.warn") + currentGoal.getGoalName() + "?",
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

            String template = LanguageManagerUlti.get(
                    "SetGoalPopup.status.goal.load.error",
                    "Error loading goal: %s"
            );

            String message = String.format(template, e.getMessage());
            showError(message);

//            statusLabel.setText("Lỗi load mục tiêu: " + e.getMessage());
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
        targetDateLabel.setText(LanguageManagerUlti.get("SetGoalPopup.label.current.goal.date") + goal.getTargetDate());

        if (goal.isCompleted()) {
            editButton.setVisible(false);
            deleteButton.setVisible(false);
            completeButton.setVisible(false);
            String template = LanguageManagerUlti.get("SetGoalPopup.label.goal.date.range");
            String dateText = String.format(template, goal.getCreatedDate().toLocalDate(), goal.getCompletedDate());
            completedInfoLabel.setText(dateText);

//            completedInfoLabel.setText("Bắt đầu: " + goal.getCreatedDate().toLocalDate()
//                    + " - Hoàn thành: " + goal.getCompletedDate());

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
        progressLabel.setText(progress + " / " + currentGoal.getGoalAmount() + LanguageManagerUlti.get("currency.unit"));

        long spent = calculateSpending();
        double spentProgress = (double) spent / currentGoal.getSpendingLimit();
        spendingProgressBar.setProgress(Math.min(spentProgress , 1.0));
        spendingLabel.setText(spent + " / " + currentGoal.getSpendingLimit() + LanguageManagerUlti.get("currency.unit"));



        completeButton.setVisible(progress >= currentGoal.getGoalAmount());
        completeButton.setOnAction(e -> completeCurrentGoal(currentGoal));
    }




    /** -----------------------
     * CALCULATE GOAL PROGRESS (từ completedDate gần nhất)
     * ----------------------- */
    private long calculateProgress(Goal goal) {
        if (goal == null) return 0;

        final LocalDateTime StartDateTime = goal.getCreatedDate();

        // Tính progress từ các giao dịch
        double progressDouble = transactionRepo.findAllCached(currentUserId).stream()
                .filter(t -> t.getCreatedAt() != null)
                .filter(t -> !t.getCreatedAt().isBefore(StartDateTime))
                .mapToDouble(Transaction::getAmount)
                .sum();

        return (long) progressDouble;
    }

    /** -----------------------
     * CALCULATE SPENDING (từ completedDate gần nhất)
     * ----------------------- */
    private Long calculateSpending() {
        if (currentGoal == null) return (long) 0;
        final LocalDateTime StartDateTime = currentGoal.getCreatedDate();

        LocalDate now = LocalDate.now();
        return transactionRepo.findAllCached(currentUserId).stream()
                .filter(t -> "chi".equalsIgnoreCase(t.getType()))
                .filter(t -> t.getCreatedAt() != null)
                .filter(t -> !t.getCreatedAt().isBefore(StartDateTime))
                .mapToLong(t -> (long) t.getAmount())
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
            if (name.isEmpty()) {
                showError(LanguageManagerUlti.get(
                        "SetGoalPopup.validation.name.empty",
                        "Tên mục tiêu không được để trống!"
                ));
                goalNameField.requestFocus();
                return;
            }

            if (amountStr.isEmpty()) {
                showError(LanguageManagerUlti.get(
                        "SetGoalPopup.validation.amount.empty",
                        "Số tiền mục tiêu không được để trống!"
                ));
                goalAmountField.requestFocus();
                return;
            }

            if (limitStr.isEmpty()) {
                showError(LanguageManagerUlti.get(
                        "SetGoalPopup.validation.limit.empty",
                        "Giới hạn chi tiêu không được để trống!"
                ));
                spendingLimitField.requestFocus();
                return;
            }

            if (targetDate == null) {
                showError(LanguageManagerUlti.get(
                        "SetGoalPopup.validation.date.empty",
                        "Vui lòng chọn thời hạn!"
                ));
                goalDatePicker.requestFocus();
                return;
            }


            long amount, limit;
            try {
                amount = Long.parseLong(amountStr);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showError(LanguageManagerUlti.get(
                        "SetGoalPopup.validation.amount.invalid",
                        "Số tiền mục tiêu phải >0!"
                ));
                goalAmountField.requestFocus();
                return;
            }

            try {
                limit = Long.parseLong(limitStr);
                if (limit <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showError(LanguageManagerUlti.get(
                        "SetGoalPopup.validation.limit.invalid",
                        "Giới hạn chi tiêu phải >0!"
                ));
                spendingLimitField.requestFocus();
                return;
            }

            if (targetDate.isBefore(today)) {
                showError(LanguageManagerUlti.get(
                        "SetGoalPopup.validation.date.beforeToday",
                        "Ngày kết thúc mục tiêu phải từ hôm nay trở đi!"
                ));
                goalDatePicker.requestFocus();
                return;
            }


            // Ngày bắt đầu dựa vào completedDate trước
//            Goal lastCompletedGoal = goalRepo.getGoalsByUserCached(currentUserId).stream()
//                    .filter(Goal::isCompleted)
//                    .filter(g -> g.getCompletedDate() != null)
//                    .max(Comparator.comparing(Goal::getCompletedDate))
//                    .orElse(null);
//
//            LocalDate startDate = (lastCompletedGoal != null) ? lastCompletedGoal.getCompletedDate().toLocalDate().plusDays(1) : today;
//            LocalDateTime goalCreatedDate = (today.isAfter(startDate) ? today.atStartOfDay() : startDate.atStartOfDay());
            LocalDateTime goalCreatedDate = LocalDateTime.now();

            // SAVE / UPDATE
            if (currentGoal == null) {
                Goal g = new Goal(0, currentUserId, name, amount, limit,
                        goalCreatedDate, targetDate, 0, 0, null);
                goalRepo.addGoal(g);
                showInfo(LanguageManagerUlti.get(
                        "SetGoalPopup.status.goal.added",
                        "Đã tạo mục tiêu mới!"));
                currentGoal = g;
            } else {
                currentGoal.setGoalName(name);
                currentGoal.setGoalAmount(amount);
                currentGoal.setSpendingLimit(limit);
                currentGoal.setTargetDate(targetDate);
//                currentGoal.setCreatedDate(goalCreatedDate);
                goalRepo.updateGoal(currentGoal);
                showInfo(LanguageManagerUlti.get(
                        "SetGoalPopup.status.goal.updated",
                        "Đã cập nhật mục tiêu!"
                ));
            }

            clearForm();
            refreshProgress();
            loadGoals();
            GoalMonitor.resetPopups();

        } catch (SQLException e) {
            String msg = String.format(
                    LanguageManagerUlti.get("SetGoalPopup.status.goal.save.error", "Lỗi khi lưu mục tiêu: %s"),
                    e.getMessage()
            );
            showError(msg);
        } catch (Exception e) {
            String msg = String.format(
                    LanguageManagerUlti.get("SetGoalPopup.status.goal.general.error", "Lỗi: %s"),
                    e.getMessage()
            );
            showError(msg);
        }

    }


    private void clearForm() {
        goalNameField.clear();
        goalAmountField.clear();
        spendingLimitField.clear();
        goalDatePicker.setValue(null);
    }




    public void displayCompletedGoals(List<Goal> goals) {
        // Formatter dùng để định dạng ngày, fallback nếu ngày null
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        completedGoalsList.getItems().clear();

        // Custom cell factory để hiển thị Node đẹp
        completedGoalsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                setGraphic(createGoalNode(item));
            }
        });

        for (Goal g : goals) {
            if (g.isCompleted()) {

                // Lấy ngày bắt đầu và ngày hoàn thành, fallback về "Chưa hoàn thành"
                String created = g.getCreatedDate() != null
                        ? g.getCreatedDate().toLocalDate().format(formatter)
                        : LanguageManagerUlti.get("SetGoalPopup.text.not.completed");

                String completed = g.getCompletedDate() != null
                        ? g.getCompletedDate().toLocalDate().format(formatter)
                        : LanguageManagerUlti.get("SetGoalPopup.text.not.completed");

                // Lưu dưới dạng string để cell factory dùng
                String text = g.getGoalName() + "|" + g.getGoalAmount() + "|" + created + "|" + completed;

                completedGoalsList.getItems().add(text);
            }
        }
    }

    private HBox createGoalNode(String data) {
        String[] parts = data.split("\\|");
        String name = parts[0];
        String amount = parts[1];
        String created = parts[2];
        String completed = parts[3];

        Label title = new Label(name);
        title.getStyleClass().add("goal-title");

        Label money = new Label(amount + LanguageManagerUlti.get("currency.unit"));
        money.getStyleClass().add("goal-money");

        // ⭐ Lấy template từ JSON và dùng String.format
        // JSON: "SetGoalPopup.label.goal.date.range": "Bắt đầu: %s | Hoàn thành: %s"
        String template = LanguageManagerUlti.get("SetGoalPopup.label.goal.date.range");
        String dateText = String.format(template, created, completed);

        Label dates = new Label(dateText);
        dates.getStyleClass().add("goal-dates");

        VBox box = new VBox(title, money, dates);
        box.getStyleClass().add("goal-box");

        HBox wrapper = new HBox(box);
        wrapper.getStyleClass().add("goal-wrapper");

        return wrapper;
    }



    private void completeCurrentGoal(Goal goal) {
        GoalMonitor.closeAllAlerts();

        try {
            goal.setIsCompleted(1);
            goal.setCompletedDate(LocalDateTime.now());
            goalRepo.updateGoal(goal);

            showInfo(LanguageManagerUlti.get(
                    "SetGoalPopup.status.goal.completed",
                    "Goal completed successfully!"
            ));

            currentGoal = null;
            currentGoalBox.setVisible(false);
            saveButton.setDisable(false);
            loadGoals();

        } catch (SQLException e) {
            showError(String.format(
                    LanguageManagerUlti.get("SetGoalPopup.status.goal.complete.error",
                            "Error completing goal: %s"),
                    e.getMessage()
            ));
        }
    }


    private void deleteGoal(Goal goal) {
        try {
            goalRepo.deleteGoal(goal.getId());

            showInfo(LanguageManagerUlti.get(
                    "SetGoalPopup.status.goal.delete",
                    "Goal deleted successfully!"
            ));

            currentGoal = null;
            currentGoalBox.setVisible(false);
            saveButton.setDisable(false);
            loadGoals();

        } catch (SQLException e) {
            showError(String.format(
                    LanguageManagerUlti.get("SetGoalPopup.status.goal.delete.error",
                            "Error deleting goal: %s"),
                    e.getMessage()
            ));
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    public void bindTexts() {
        titleLabel.setText(LanguageManagerUlti.get("SetGoalPopup.label.title"));
        goalNameField.setPromptText(LanguageManagerUlti.get("SetGoalPopup.textfield.goal.name.input"));
        goalAmountField.setPromptText(LanguageManagerUlti.get("SetGoalPopup.textfield.goal.amount.input"));
        goalLabel.setText(LanguageManagerUlti.get("SetGoalPopup.label.goal.name"));
        goalAmountLabel.setText(LanguageManagerUlti.get("SetGoalPopup.label.goal.amount"));
        goalDateLabel.setText(LanguageManagerUlti.get("SetGoalPopup.label.goal.date"));
        spendingLimitLabel.setText(LanguageManagerUlti.get("SetGoalPopup.label.spending.limit"));
        saveButton.setText(LanguageManagerUlti.get("SetGoalPopup.button.save.goal"));

        progressTitleLabel.setText(LanguageManagerUlti.get("SetGoalPopup.label.progress.title"));
        progressLabel.setText(LanguageManagerUlti.get("SetGoalPopup.label.progress.info"));

        spendingTitleLabel.setText(LanguageManagerUlti.get("SetGoalPopup.label.spending.title"));
        spendingLabel.setText(LanguageManagerUlti.get("SetGoalPopup.label.spending.info"));

        editButton.setText(LanguageManagerUlti.get("SetGoalPopup.button.edit.goal"));
        deleteButton.setText(LanguageManagerUlti.get("SetGoalPopup.button.delete.goal"));
        completeButton.setText(LanguageManagerUlti.get("SetGoalPopup.button.complete.goal"));

        completedGoalsTitle.setText(LanguageManagerUlti.get("SetGoalPopup.label.completed.goals.title"));
    }



}
