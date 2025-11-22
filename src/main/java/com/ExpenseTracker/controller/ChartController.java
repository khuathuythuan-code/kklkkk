package com.ExpenseTracker.controller;

import com.ExpenseTracker.model.Transaction;
import com.ExpenseTracker.repository.CategoryRepository;
import com.ExpenseTracker.repository.TransactionRepository;
import com.ExpenseTracker.repository.UserRepository;
import com.ExpenseTracker.utility.ChangeSceneUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.BreakIterator;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ChartController implements Initializable {
    private final TransactionRepository repo = new TransactionRepository();
    private final CategoryRepository catRepo = new CategoryRepository();
    private final int currentUserId = UserRepository.currentUserID; // demo; thay bằng session User

    @FXML private ComboBox<Integer> statsMonthComboBox, statsYearComboBox;
    @FXML private PieChart expensePieChart;
    @FXML private PieChart revenuePieChart;
    @FXML private javafx.scene.chart.BarChart<String, Number> barChart;
    @FXML private Button yearToggleBtn, monthToggleBtn;
    @FXML private Label expenseLabel, incomeLabel, totalLabel;

    private boolean isMonthlyChart = true;
    private boolean firstLoad = true;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        int curYear = LocalDate.now().getYear();
        for (int m=1;m<=12;m++) {
            statsMonthComboBox.getItems().add(m);
        }
        for (int y = curYear-5; y <= curYear+1; y++) {
            statsYearComboBox.getItems().add(y);
        }

        statsMonthComboBox.setValue(LocalDate.now().getMonthValue());
        statsYearComboBox.setValue(curYear);

        statsYearComboBox.setOnAction(e-> updateCharts());
        statsMonthComboBox.setOnAction(e-> updateCharts());

        yearToggleBtn.setOnAction(e-> switchYearMonthData(false));
        monthToggleBtn.setOnAction(e-> switchYearMonthData(true));

        barChart.setVisible(false);
        updateCharts();
    }


    public void switchYearMonthData(boolean isMonthData){
        if(isMonthlyChart != isMonthData){
            isMonthlyChart = isMonthData;
            updateCharts();
            barChart.setVisible(!isMonthlyChart);
            statsMonthComboBox.setDisable(!isMonthlyChart);
            if (isMonthlyChart) {
                monthlySummary();
            } else {
                yearSummary();
            }
        }
    }


    public void updateCharts() {
        List<Transaction> annualData = repo.findByYear(currentUserId, statsYearComboBox.getValue());
        List<Transaction> monthlyData = repo.findByMonth(currentUserId, statsMonthComboBox.getValue(), statsYearComboBox.getValue());
        List<Transaction> data = isMonthlyChart ? monthlyData : annualData;

        Map<String, Double> revenueCate = data.stream()
                .filter(t -> "Thu".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));

        Map<String, Double> expenseCate = data.stream()
                .filter(t -> "Chi".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));

        // Tắt animation PieChart để tránh mất PieChart lần đầu
        revenuePieChart.setAnimated(false);
        expensePieChart.setAnimated(false);

        // Cập nhật dữ liệu PieChart
        revenuePieChart.getData().clear();
        revenueCate.forEach((k, v) -> revenuePieChart.getData().add(new PieChart.Data(k, v)));

        expensePieChart.getData().clear();
        expenseCate.forEach((k, v) -> expensePieChart.getData().add(new PieChart.Data(k, v)));

        // BarChart Thu & Chi theo tháng
        barChart.getData().clear();
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Thu");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Chi");

        for (int m = 1; m <= 12; m++) {
            final int month = m;
            double thu = annualData.stream()
                    .filter(t -> "Thu".equalsIgnoreCase(t.getType()) &&
                            t.getCreatedAt().getMonthValue() == month)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            double chi = annualData.stream()
                    .filter(t -> "Chi".equalsIgnoreCase(t.getType()) &&
                            t.getCreatedAt().getMonthValue() == month)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            incomeSeries.getData().add(new XYChart.Data<>(String.valueOf(month), thu));
            expenseSeries.getData().add(new XYChart.Data<>(String.valueOf(month), chi));
        }

        barChart.getData().addAll(incomeSeries, expenseSeries);

        // Cập nhật summary
        if (isMonthlyChart) {
            monthlySummary();
        } else {
            yearSummary();
        }
    }



    private void monthlySummary(){
        List<Transaction> all = repo.findByMonth(currentUserId,statsMonthComboBox.getValue(),statsYearComboBox.getValue());

        // ==== 1. Tính tổng thu, chi, tổng cộng ====
        double income = all.stream()
                .filter(t -> "Thu".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double expense = all.stream()
                .filter(t -> "Chi".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        incomeLabel.setText(String.format("%,.0f", income));
        expenseLabel.setText(String.format("%,.0f", Math.abs(expense)));
        totalLabel.setText(String.format("%,.0f", income + expense)); // hoặc income - expense nếu bạn muốn chênh lệch thực tế
    }

    private void yearSummary(){
        List<Transaction> all = repo.findByYear(currentUserId,statsYearComboBox.getValue());

        // ==== 1. Tính tổng thu, chi, tổng cộng ====
        double income = all.stream()
                .filter(t -> "Thu".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double expense = all.stream()
                .filter(t -> "Chi".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        incomeLabel.setText(String.format("%,.0f", income));
        expenseLabel.setText(String.format("%,.0f", Math.abs(expense)));
        totalLabel.setText(String.format("%,.0f", income + expense)); // hoặc income - expense nếu bạn muốn chênh lệch thực tế
    }



    @FXML
    private void changeScene(ActionEvent e) throws IOException {
        Button btn = (Button) e.getSource();
        String id = btn.getId();

        String fxml = switch (id) {
            case "btnHome" -> "/fxml/main-expense.fxml";
            case "btnHistory" -> "/fxml/history.fxml";
            case "btnReport" -> "/fxml/chart.fxml";
            case "btnSettings" -> "/fxml/settings.fxml";
            default -> throw new IllegalArgumentException("Không xác định nút: " + id);
        };

        Stage stage = (Stage) btn.getScene().getWindow();
        ChangeSceneUtil.navigate(stage, fxml);
    }

}
