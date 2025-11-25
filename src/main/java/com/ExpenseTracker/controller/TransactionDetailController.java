package com.ExpenseTracker.controller;

import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.TransactionUpdateListener;
import com.ExpenseTracker.model.Transaction;
import com.ExpenseTracker.repository.CategoryRepository;
import com.ExpenseTracker.repository.TransactionRepository;
import com.ExpenseTracker.utility.LanguageManagerUlti;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class TransactionDetailController {
    @FXML
    private TextField inputMoneyField;
    @FXML private DatePicker inputDateField;
    @FXML private TextArea inputNoteField;
    @FXML private TextField categoryField;
    @FXML private Button updateBtn, deleteBtn, closeBtn;
    @FXML private Label titleLabel;
    @FXML private Label moneyLabel;
    @FXML private Label dateLabel;
    @FXML private Label categoryLabel;
    @FXML private Label noteLabel;

    private Transaction transaction;
    private final TransactionRepository repo = new TransactionRepository();
    private TransactionUpdateListener listener;

    public void setListener(TransactionUpdateListener listener) {
        this.listener = listener;
    }

    @FXML
    private void initialize() {
        // Cập nhật locale
        LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage);

        // Cập nhật text cho UI
        bindTexts();
    }


    public void setTransaction(Transaction t){
        this.transaction = t;
        // Hiển thị số tiền bình thường, không scientific notation
        inputMoneyField.setText(String.format("%.0f", Math.abs(t.getAmount())));
        inputDateField.setValue(t.getCreatedAt().toLocalDate());
        inputNoteField.setText(t.getNote());
        categoryField.setText(t.getCategory());
    }


    @FXML private void updateTransaction(){
        try {
            // Đọc số tiền từ TextField, dùng double thay vì float
            double amount = Double.parseDouble(inputMoneyField.getText());
            if ("Chi".equalsIgnoreCase(transaction.getType())) amount = -Math.abs(amount);
            else amount = Math.abs(amount);
            transaction.setAmount((float) amount);

            transaction.setNote(inputNoteField.getText());
            LocalDate ld = inputDateField.getValue();
            transaction.setCreatedAt(LocalDateTime.of(ld, LocalTime.now()));
            transaction.setCategory(categoryField.getText());
            repo.update(transaction);

            if (listener != null) listener.onTransactionUpdated(transaction);

            closeWindow();
        } catch(Exception e) {
            new Alert(Alert.AlertType.ERROR, "Dữ liệu không hợp lệ").showAndWait();
        }
    }


    @FXML private void deleteTransaction(){
        repo.delete(transaction.getId(), transaction.getUserId());

        // 🟢 Gọi callback
        if (listener != null) listener.onTransactionDeleted(transaction);

        closeWindow();
    }

    @FXML private void closeWindow(){
        Stage s = (Stage) closeBtn.getScene().getWindow();
        s.close();
    }

    private void bindTexts() {
        titleLabel.setText(LanguageManagerUlti.get("TransactionDetail.label.title"));
        moneyLabel.setText(LanguageManagerUlti.get("TransactionDetail.label.money"));
        dateLabel.setText(LanguageManagerUlti.get("TransactionDetail.label.date"));
        categoryLabel.setText(LanguageManagerUlti.get("TransactionDetail.label.category"));
        noteLabel.setText(LanguageManagerUlti.get("TransactionDetail.label.note"));

        updateBtn.setText(LanguageManagerUlti.get("TransactionDetail.button.update"));
        deleteBtn.setText(LanguageManagerUlti.get("TransactionDetail.button.delete"));
        closeBtn.setText(LanguageManagerUlti.get("TransactionDetail.button.close"));
    }

}
