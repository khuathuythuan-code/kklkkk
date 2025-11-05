package org.example.project2ver2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.project2ver2.model.Transaction;
import org.example.project2ver2.repository.TransactionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TransactionDetailController {
    @FXML private TextField inputMoneyField;
    @FXML private DatePicker inputDateField;
    @FXML private TextArea inputNoteField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private Button updateBtn, deleteBtn, closeBtn;

    private Transaction transaction;
    private final TransactionRepository repo = new TransactionRepository();

    public void setTransaction(Transaction t){
        this.transaction = t;
        inputMoneyField.setText(String.valueOf(Math.abs(t.getAmount())));
        inputDateField.setValue(t.getCreatedAt().toLocalDate());
        inputNoteField.setText(t.getNote());
        // load categories (could inject)
        categoryCombo.getItems().addAll("Ăn uống","Di chuyển","Mua sắm","Học tập","Giải trí","Khác");
        categoryCombo.setValue(t.getCategory());
    }

    @FXML private void updateTransaction(){
        try {
            float amount = Float.parseFloat(inputMoneyField.getText());
            if ("Chi".equalsIgnoreCase(transaction.getType())) amount = -Math.abs(amount);
            else amount = Math.abs(amount);
            transaction.setAmount(amount);
            transaction.setNote(inputNoteField.getText());
            LocalDate ld = inputDateField.getValue();
            transaction.setCreatedAt(LocalDateTime.of(ld, LocalTime.now()));
            transaction.setCategory(categoryCombo.getValue());
            repo.update(transaction);
            closeWindow();
        } catch(Exception e) {
            new Alert(Alert.AlertType.ERROR, "Dữ liệu không hợp lệ").showAndWait();
        }
    }

    @FXML private void deleteTransaction(){
        repo.delete(transaction.getId());
        closeWindow();
    }

    @FXML private void closeWindow(){
        Stage s = (Stage) closeBtn.getScene().getWindow();
        s.close();
    }
}
