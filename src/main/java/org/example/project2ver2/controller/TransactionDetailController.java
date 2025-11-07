package org.example.project2ver2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.project2ver2.model.Transaction;
import org.example.project2ver2.repository.CategoryRepository;
import org.example.project2ver2.repository.TransactionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class TransactionDetailController {
    @FXML private TextField inputMoneyField;
    @FXML private DatePicker inputDateField;
    @FXML private TextArea inputNoteField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private Button updateBtn, deleteBtn, closeBtn;


    private Transaction transaction;
    private final TransactionRepository repo = new TransactionRepository();
    private CategoryRepository categoryRepository = new CategoryRepository();
    private TransactionUpdateListener listener;

    public void setListener(TransactionUpdateListener listener) {
        this.listener = listener;
    }

    public void setTransaction(Transaction t){

        this.transaction = t;
        inputMoneyField.setText(String.valueOf(Math.abs(t.getAmount())));
        inputDateField.setValue(t.getCreatedAt().toLocalDate());
        inputNoteField.setText(t.getNote());
        List<String> cats = categoryRepository.findAll(t.getUserId(),t.getType());

        // load categories (could inject)
        if(cats.isEmpty()) {
            cats = Arrays.asList("Ăn uống","Di chuyển","Mua sắm","Học tập","Giải trí","Khác");
        }
        categoryCombo.getItems().addAll(cats);
        if (!cats.contains(t.getCategory())){
             cats.add(t.getCategory());
        }
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

            // 🟢 Gọi callback
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


}
