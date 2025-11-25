package com.ExpenseTracker.controller;

import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.repository.CategoryRepository;
import com.ExpenseTracker.utility.LanguageManagerUlti;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {
    @FXML
    private Label title;
    @FXML private ListView<String> categoryList;
    @FXML private TextField newCategoryField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Button addCate, deleteCate, closeDialog;

    private final int currentUserId = Singleton.getInstance().currentUser;
    private CategoryRepository repository = new CategoryRepository();

    private ObservableList<String> categories = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Cập nhật locale
        LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage);

        // Cập nhật text cho UI
        bindTexts();

        if (Singleton.getInstance().currentLanguage.equalsIgnoreCase("en")){
            typeCombo.getItems().addAll("Expense", "Income");
            typeCombo.setValue("Expense");
        }else {
            typeCombo.getItems().addAll("Chi", "Thu");
            typeCombo.setValue("Chi");
        }

        categoryList.setItems(categories); // bind ListView với ObservableList

        loadCategories();

        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadCategories();
        });

        addCate.setOnAction(e->addCategory());
        deleteCate.setOnAction(e->deleteSelected());
        closeDialog.setOnAction(e->closeDialog());
    }

    private void loadCategories() {
        categories.setAll(repository.findCategories(currentUserId,typeCombo.getValue()));
    }

    @FXML
    private void addCategory() {
        String name = newCategoryField.getText();
        String type;
        if (typeCombo.getValue().equalsIgnoreCase("expense")){
            type = "Chi";
        }else{
            type = "Thu";
        }


        if (name == null || name.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Tên danh mục trống").showAndWait();
            return;
        }

        repository.addCategory(currentUserId,name, type);
        categories.add(name); // thêm trực tiếp vào ObservableList
        newCategoryField.clear();
    }

    @FXML
    private void deleteSelected() {
        String sel = categoryList.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        String name = sel.replaceAll("\\s*\\(.*\\)$", "").trim();
        repository.deleteCategory(currentUserId,name);
        categories.remove(sel); // xoá trực tiếp từ ObservableList
    }

    @FXML
    private void closeDialog() {
        Stage s = (Stage) categoryList.getScene().getWindow();
        s.close();
    }

    private void bindTexts() {
        title.setText(LanguageManagerUlti.get("Category.label.title"));
        newCategoryField.setPromptText(LanguageManagerUlti.get("Category.textfield.new.category.input"));
        addCate.setText(LanguageManagerUlti.get("Category.button.add.category"));
        deleteCate.setText(LanguageManagerUlti.get("Category.button.delete.category"));
        closeDialog.setText(LanguageManagerUlti.get("Category.button.close.dialog"));
    }


}
