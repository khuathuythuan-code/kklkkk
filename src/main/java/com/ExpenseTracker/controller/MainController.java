package com.ExpenseTracker.controller;

import com.ExpenseTracker.GoalMonitor;
import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.model.Transaction;
import com.ExpenseTracker.repository.CategoryRepository;
import com.ExpenseTracker.repository.TransactionRepository;
import com.ExpenseTracker.utility.ChangeSceneUtil;
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
            saveTransBtn, editCategoryBtn, goalSettingButton, incomeToggle, expenseToggle;
    @FXML private ComboBox<String> catergoryComboBox, methodComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextArea noteTextArea;
    @FXML private TextField inputMoneyTextField;
    @FXML private Parent mainPane;
    @FXML private ProgressBar goalSupervisorBar;
    @FXML private Label goalSupervisorLabel;
    @FXML private HBox instantExpenseList, instantIncomeList;


    private final TransactionRepository repo = new TransactionRepository();
    private final CategoryRepository catRepo = new CategoryRepository();
    private List<Pair<Node, Node>> pairs;
    private int currentUserId = Singleton.getInstance().currentUser;
    private Boolean isExpense = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // --- Toggle Thu / Chi ---
        incomeToggle.setOnAction(e -> { isExpense = false; refreshCategories(); });
        expenseToggle.setOnAction(e -> { isExpense = true; refreshCategories(); });

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
        catergoryComboBox.setOnAction(e -> { updateButtonText(catergoryBtn, "Danh mục", catergoryComboBox.getValue()); resetAll(); });
        methodComboBox.setOnAction(e -> { updateButtonText(methodBtn, "Phương thức", methodComboBox.getValue()); resetAll(); });
        datePicker.setOnAction(e -> { updateButtonText(dateBtn, "Ngày", datePicker.getValue().toString()); resetAll(); });
        noteTextArea.setOnKeyReleased(e -> updateButtonText(noteBtn, "Ghi chú", noteTextArea.getText()));
        inputMoneyTextField.setOnKeyReleased(e -> updateButtonText(inputMoneyBtn, "Số tiền", inputMoneyTextField.getText()));

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
            // nếu là chi
            if (isExpense) {
                cats = Arrays.asList("Ăn uống", "Di chuyển", "Mua sắm", "Học tập", "Giải trí", "Khác");

            } else {
                cats = Arrays.asList("Lương", "Bất động sản", "Bán Hàng", "Giao hàng", "Trợ cấp", "Khác");

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
            amount = isExpense ? -Math.abs(amount) : Math.abs(amount);

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
            new Alert(Alert.AlertType.ERROR, "Số tiền không hợp lệ").showAndWait();
        }
    }

    private void notifySuccessfulTrans() {
        inputMoneyBtn.setText("Lưu thành công");
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> inputMoneyBtn.setText("Số tiền: 0"));
        pause.play();
    }

    private void setUpField() {
        methodComboBox.getItems().setAll("Tiền mặt","Thẻ","Ví điện tử");
        methodComboBox.setValue("Tiền mặt");
        datePicker.setValue(LocalDate.now());
        noteTextArea.clear();
        inputMoneyTextField.setText("");

        updateButtonText(catergoryBtn, "Danh mục", catergoryComboBox.getValue());
        updateButtonText(methodBtn, "Phương thức", methodComboBox.getValue());
        updateButtonText(dateBtn, "Ngày", datePicker.getValue().toString());
        updateButtonText(noteBtn, "Ghi chú", "");
        updateButtonText(inputMoneyBtn, "Số tiền", "0 đ");
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
        btn.setText(label + ": " + value);
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
            Stage st = new Stage();
            if (!btn.getId().equalsIgnoreCase("calculator")) st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(loader.load()));
            st.showAndWait();
            refreshCategories();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

}
