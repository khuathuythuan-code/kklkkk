package com.ExpenseTracker.controller;

import com.ExpenseTracker.GoalMonitor;
import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.model.Transaction;
import com.ExpenseTracker.repository.CategoryRepository;
import com.ExpenseTracker.repository.TransactionRepository;
import com.ExpenseTracker.utility.ChangeSceneUtil;
import com.ExpenseTracker.utility.LanguageManagerUlti;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Button catergoryBtn, dateBtn, methodBtn, noteBtn, inputMoneyBtn,
            saveTransBtn, editCategoryBtn, goalSettingButton, incomeToggle, expenseToggle, calculator;
    @FXML private ComboBox<String> catergoryComboBox, methodComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextArea noteTextArea;
    @FXML private TextField inputMoneyTextField;
    @FXML private Parent mainPane;
    @FXML private ProgressBar goalSupervisorBar;
    @FXML private Label goalSupervisorLabel;
    @FXML private HBox instantExpenseList, instantIncomeList;
    @FXML private Button btnHome;
    @FXML private Button btnHistory;
    @FXML private Button btnReport;
    @FXML private Button btnSettings;


    private final TransactionRepository repo = new TransactionRepository();
    private final CategoryRepository catRepo = new CategoryRepository();
    private List<Pair<Node, Node>> pairs;
    private int currentUserId = Singleton.getInstance().currentUser;
    private Boolean isExpense = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // --- Thiết lập ngôn ngữ mặc định ---
        LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage);
        bindTexts();

        // --- Toggle Thu / Chi ---


        expenseToggle.setOnAction(e -> {
            // Thêm class "active" nếu chưa có
            if (!expenseToggle.getStyleClass().contains("active")) {
                expenseToggle.getStyleClass().add("active");
            }
            // Xóa class "active" của nút kia nếu còn
            incomeToggle.getStyleClass().remove("active");

            isExpense = true;
            refreshCategories(); // Luôn chạy khi click
        });

        incomeToggle.setOnAction(e -> {
            if (!incomeToggle.getStyleClass().contains("active")) {
                incomeToggle.getStyleClass().add("active");
            }
            expenseToggle.getStyleClass().remove("active");

            isExpense = false;
            refreshCategories(); // Luôn chạy khi click
        });



        // --- Button <-> Field ---
        pairs = List.of(
                new Pair<>(catergoryBtn, catergoryComboBox),
                new Pair<>(dateBtn, datePicker),
                new Pair<>(methodBtn, methodComboBox),
                new Pair<>(noteBtn, noteTextArea),
                new Pair<>(inputMoneyBtn, inputMoneyTextField)
        );
        pairs.forEach(pair -> {
            Node btn = pair.getKey();
            Node field = pair.getValue();
            btn.setOnMouseClicked(e -> { showField(btn, field); e.consume(); });
        });
        mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            boolean clickOutside = pairs.stream()
                    .noneMatch(pair -> pair.getKey().isHover() || pair.getValue().isHover());
            if (clickOutside) resetAll();
        });

        // --- Cập nhật button khi chọn / nhập ---
        catergoryComboBox.setOnAction(e -> { updateButtonText(catergoryBtn, LanguageManagerUlti.get("main.button.category.select"), catergoryComboBox.getValue()); resetAll(); });
        methodComboBox.setOnAction(e -> { updateButtonText(methodBtn, LanguageManagerUlti.get("main.button.method.select"), methodComboBox.getValue()); resetAll(); });
        datePicker.setOnAction(e -> { updateButtonText(dateBtn, LanguageManagerUlti.get("main.button.date.select"), datePicker.getValue().toString()); resetAll(); });
        noteTextArea.setOnKeyReleased(e -> updateButtonText(noteBtn, LanguageManagerUlti.get("main.button.note.select"), noteTextArea.getText()));
        inputMoneyTextField.setOnKeyReleased(e -> updateButtonText(inputMoneyBtn,LanguageManagerUlti.get("currency.unit"), inputMoneyTextField.getText()));

        // --- Dialogs ---
        editCategoryBtn.setOnAction(this::openDialog);
        goalSettingButton.setOnAction(this::openDialog);

        // --- Lưu transaction ---
        saveTransBtn.setOnAction(e -> {
            createTransaction();
            resetAll();
        });

        // --- Init ---
        refreshCategories();
        setUpField();
        resetAll();

        // --- Bind ProgressBar + Label ---
        Singleton.getInstance().goalSupervisorBar.addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                goalSupervisorBar.setProgress(newVal.doubleValue());
                goalSupervisorLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
            });
        });

        // --- Bind ProgressBar ---
        GoalMonitor.registerProgressBar(goalSupervisorBar, goalSupervisorLabel);

        // --- Start GoalMonitor ---
        GoalMonitor.start(currentUserId);

        // ✅ Cập nhật ngay ProgressBar
        GoalMonitor.updateProgressBarNow();
    }


    private void refreshCategories() {
        String typeCate = isExpense ? "chi" : "thu";
        List<String> cats = catRepo.findCategories(currentUserId, typeCate);

        if (cats.isEmpty()) {
            if(Singleton.getInstance().currentLanguage.equalsIgnoreCase("vi")){
                if (isExpense) {
                    cats = Arrays.asList("Ăn uống", "Di chuyển", "Mua sắm", "Học tập", "Giải trí", "Khác");
                } else {
                    cats = Arrays.asList("Lương", "Bất động sản", "Bán Hàng", "Giao hàng", "Trợ cấp", "Khác");
                }
            }else {
                if (isExpense) {
                    cats = Arrays.asList("Food & Drink", "Transport", "Shopping", "Study", "Entertainment", "Other");
                } else {
                    cats = Arrays.asList("Salary", "Real Estate", "Sales", "Delivery", "Allowance", "Other");
                }
            }
        }

        if (isExpense) {
            instantIncomeList.setVisible(false);
            instantIncomeList.setManaged(false);
            instantExpenseList.setVisible(true);
            instantExpenseList.setManaged(true);
        } else {
            instantIncomeList.setVisible(true);
            instantIncomeList.setManaged(true);
            instantExpenseList.setVisible(false);
            instantExpenseList.setManaged(false);
        }

        catergoryComboBox.getItems().setAll(cats);
        catergoryComboBox.setValue(cats.get(0));
    }

    private void clearInput() {
        inputMoneyTextField.clear();
        noteTextArea.clear();
        datePicker.setValue(LocalDate.now());
    }

    private void createTransaction() {
        try {
            float amount = Float.parseFloat(inputMoneyTextField.getText());
//            amount = isExpense ? -Math.abs(amount) : Math.abs(amount);

            Transaction t = new Transaction();
            t.setUserId(currentUserId);
            t.setAmount(amount);
            t.setCategory(catergoryComboBox.getValue());
            t.setNote(noteTextArea.getText());
            t.setType(isExpense ? "Chi" : "Thu");
            t.setCreatedAt(LocalDateTime.of(datePicker.getValue(), LocalTime.now()));
            t.setTransMethod(methodComboBox.getValue());
            repo.add(t);

            notifySuccessfulTrans();
            clearInput();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, LanguageManagerUlti.get("main.alert.invalid.amount")).showAndWait();
        }
    }

    private void notifySuccessfulTrans() {
        inputMoneyBtn.setText(LanguageManagerUlti.get("main.button.transaction.saved"));
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> inputMoneyBtn.setText(LanguageManagerUlti.get("main.button.transaction.amount") + ": 0"));
        pause.play();
    }


    private void setUpField() {
        methodComboBox.getItems().setAll(LanguageManagerUlti.get("main.method.cash"),
                LanguageManagerUlti.get("main.method.card"),
                LanguageManagerUlti.get("main.method.e-wallet"));
        methodComboBox.setValue(LanguageManagerUlti.get("main.method.cash"));
        datePicker.setValue(LocalDate.now());
        noteTextArea.clear();
        inputMoneyTextField.setText("");

        updateButtonText(catergoryBtn, LanguageManagerUlti.get("main.button.category.select"), catergoryComboBox.getValue());
        updateButtonText(methodBtn, LanguageManagerUlti.get("main.button.method.select"), methodComboBox.getValue());
        updateButtonText(dateBtn, LanguageManagerUlti.get("main.button.date.select"), datePicker.getValue().toString());
        updateButtonText(noteBtn, LanguageManagerUlti.get("main.button.note.select"), "");
        updateButtonText(inputMoneyBtn, LanguageManagerUlti.get("currency.unit"), "0");
    }

    private void showField(Node btn, Node field) {
        resetAll();
        btn.setVisible(false); btn.setManaged(false);
        field.setVisible(true); field.setManaged(true);
        field.requestFocus();

        if (field instanceof ComboBox<?> combo) combo.show();
        if (field instanceof DatePicker dp) dp.show();
    }

    private void resetAll() {
        pairs.forEach(pair -> {
            Node btn = pair.getKey(); Node field = pair.getValue();
            btn.setVisible(true); btn.setManaged(true);
            field.setVisible(false); field.setManaged(false);
        });
    }

    private void updateButtonText(Button btn, String label, String value) {
        if (value == null || value.isBlank()) value = "";
        int maxLength = 15;
        if (value.length() > maxLength) value = value.substring(0, maxLength) + "...";
        if (btn == inputMoneyBtn){
            btn.setText(value +" "+ label);
        }else {
            btn.setText(label + ": " + value);
        }
    }

    @FXML
    private void changeScene(ActionEvent e) throws IOException {
        Button btn = (Button) e.getSource();
        String fxml = switch (btn.getId()) {
            case "btnHome" -> "/fxml/main-expense.fxml";
            case "btnHistory" -> "/fxml/history.fxml";
            case "btnReport" -> "/fxml/chart.fxml";
            case "btnSettings" -> "/fxml/settings.fxml";
            default -> throw new IllegalArgumentException("Không xác định nút: " + btn.getId());
        };
        Stage stage = (Stage) btn.getScene().getWindow();
        ChangeSceneUtil.navigate(stage, fxml);
    }

    @FXML
    private void openDialog(ActionEvent e) {
        Button btn = (Button) e.getSource();
        String fxml = switch (btn.getId()) {
            case "goalSettingButton" -> "/fxml/set-goal-popup.fxml";
            case "editCategoryBtn" -> "/fxml/category_dialog.fxml";
            case "calculator" -> "/fxml/calculator-popup.fxml";
            default -> throw new IllegalArgumentException("Không xác định nút: " + btn.getId());
        };
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Stage main = (Stage) inputMoneyBtn.getScene().getWindow();
            Stage st = new Stage();
            st.initOwner(main);
            if (!btn.getId().equalsIgnoreCase("calculator")) st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(loader.load()));
            st.showAndWait();
            refreshCategories();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void bindTexts() {
        // Nút toggle
        expenseToggle.setText(LanguageManagerUlti.get("main.button.expense.toggle"));
        incomeToggle.setText(LanguageManagerUlti.get("main.button.income.toggle"));

        // Nút mục tiêu & máy tính
        goalSettingButton.setText(LanguageManagerUlti.get("main.button.goal.setting"));
        calculator.setText(LanguageManagerUlti.get("main.button.calculator.open"));

        // Thông tin giao dịch
        catergoryBtn.setText(LanguageManagerUlti.get("main.button.category.select"));
        dateBtn.setText(LanguageManagerUlti.get("main.button.date.select"));
        methodBtn.setText(LanguageManagerUlti.get("main.button.method.select"));
        noteBtn.setText(LanguageManagerUlti.get("main.button.note.select"));

        // Nút sửa danh mục
        editCategoryBtn.setText(LanguageManagerUlti.get("main.button.category.edit"));

        // Nút lưu giao dịch
        saveTransBtn.setText(LanguageManagerUlti.get("main.button.transaction.save"));

        // Nút menu dưới cùng
        btnHome.setText(LanguageManagerUlti.get("main.button.menu.home"));
        btnHistory.setText(LanguageManagerUlti.get("main.button.menu.history"));
        btnReport.setText(LanguageManagerUlti.get("main.button.menu.report"));
        btnSettings.setText(LanguageManagerUlti.get("main.button.menu.settings"));
    }
}
