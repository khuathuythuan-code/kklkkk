package org.example.project2ver2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.project2ver2.model.Transaction;
import org.example.project2ver2.repository.TransactionRepository;
import org.example.project2ver2.repository.CategoryRepository;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class AppController implements Initializable {
    private final TransactionRepository repo = new TransactionRepository();
    private final CategoryRepository catRepo = new CategoryRepository();
    private final int currentUserId = 1; // demo; thay bằng session user

    @FXML private TextField inputMoneyField;
    @FXML private DatePicker inputDateField;
    @FXML private TextArea inputNoteField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private Button expenseBtn, inputDataBtn;

    @FXML private ComboBox<Integer> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private GridPane calendarGrid;
    @FXML private ScrollPane transcContainer;
    @FXML private Label incomeLabel, expenseLabel, totalLabel;

    @FXML private PieChart pieChart;
    @FXML private javafx.scene.chart.BarChart<String, Number> barChart;

    private boolean isExpense = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // init categories
        refreshCategories();

        // init date fields
        inputDateField.setValue(LocalDate.now());
        inputMoneyField.setPromptText("VD: 150000");

        // init month/year
        int curYear = LocalDate.now().getYear();
        for (int m=1;m<=12;m++) monthComboBox.getItems().add(m);
        for (int y = curYear-5; y <= curYear+1; y++) yearComboBox.getItems().add(y);
        monthComboBox.setValue(LocalDate.now().getMonthValue());
        yearComboBox.setValue(curYear);

        monthComboBox.setOnAction(e -> refreshCalendar());
        yearComboBox.setOnAction(e -> refreshCalendar());

        refreshCalendar();
        updateChartsAndSummary();
    }

    private void refreshCategories(){
        categoryCombo.getItems().clear();
        List<String> cats = catRepo.findAll(currentUserId);
        if (cats.isEmpty()) {
            cats = Arrays.asList("Ăn uống","Di chuyển","Mua sắm","Học tập","Giải trí","Khác");
        }
        categoryCombo.getItems().addAll(cats);
        categoryCombo.setValue(cats.get(0));
    }

    @FXML
    private void toggleType(){
        isExpense = !isExpense;
        expenseBtn.setText(isExpense ? "Chi" : "Thu");
    }

    @FXML
    private void createTransaction(){
        try {
            float amount = Float.parseFloat(inputMoneyField.getText());
            if (isExpense) amount = -Math.abs(amount); else amount = Math.abs(amount);
            Transaction t = new Transaction();
            t.setUserId(currentUserId);
            t.setAmount(amount);
            t.setCategory(categoryCombo.getValue());
            t.setNote(inputNoteField.getText());
            t.setType(isExpense ? "Chi" : "Thu");
            LocalDate ld = inputDateField.getValue();
            t.setCreatedAt(LocalDateTime.of(ld, LocalTime.now()));
            repo.add(t);
            clearInput();
            refreshCalendar();
            updateChartsAndSummary();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Số tiền không hợp lệ").showAndWait();
        }
    }

    private void clearInput(){
        inputMoneyField.clear();
        inputNoteField.clear();
        inputDateField.setValue(LocalDate.now());
    }

    private void refreshCalendar(){
        int month = monthComboBox.getValue();
        int year = yearComboBox.getValue();
        generateCalendar(month, year);
        updateChartsAndSummary();
    }

    private void generateCalendar(int month, int year) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        for (int i=0;i<7;i++){
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(100.0/7);
            calendarGrid.getColumnConstraints().add(c);
        }

        // header weekday labels
        String[] days = {"T2","T3","T4","T5","T6","T7","CN"};
        for (int i=0;i<7;i++){
            Label l = new Label(days[i]);
            l.setStyle("-fx-font-weight:bold;");
            calendarGrid.add(l, i, 0);
        }

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int firstWeekday = firstDay.getDayOfWeek().getValue(); // 1=Mon
        LocalDate prevMonth = firstDay.minusMonths(1);
        int daysInPrev = prevMonth.lengthOfMonth();
        int daysInMonth = firstDay.lengthOfMonth();

        // start cell index (row 1 because row0 is weekday header)
        int startCol = firstWeekday -1;
        int cell = 0;
        int totalCells = 6*7; // 6 rows of dates
        int dayCounterPrevStart = daysInPrev - startCol +1;

        int idx = 0;
        int row = 1;
        int col = 0;

        for (int i=0;i<totalCells;i++){
            col = i%7;
            row = i/7 + 1;
            VBox cellBox = new VBox();
            cellBox.setPrefSize(60,60);
            cellBox.setPadding(new Insets(4));
            cellBox.setStyle("-fx-border-color: transparent;");

            LocalDate date;
            boolean isCurrent;
            if (i < startCol) {
                // prev month
                date = prevMonth.withDayOfMonth(dayCounterPrevStart + i);
                isCurrent = false;
            } else if (i < startCol + daysInMonth) {
                date = firstDay.withDayOfMonth(i - startCol + 1);
                isCurrent = true;
            } else {
                date = firstDay.plusMonths(1).withDayOfMonth(i - startCol - daysInMonth + 1);
                isCurrent = false;
            }

            Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            cellBox.getChildren().add(dayLabel);

            List<Transaction> txs = repo.findByDate(currentUserId, date);
            if (!txs.isEmpty()) {
                // show small indicator and color
                HBox indicators = new HBox(4);
                long thu = txs.stream().filter(t->"Thu".equalsIgnoreCase(t.getType())).count();
                long chi = txs.stream().filter(t->"Chi".equalsIgnoreCase(t.getType())).count();
                if (thu>0) indicators.getChildren().add(new Label("↑"+thu));
                if (chi>0) indicators.getChildren().add(new Label("↓"+chi));
                cellBox.getChildren().add(indicators);
            }

            final LocalDate dateForHandler = date;
            cellBox.setOnMouseClicked(e -> showTransactionsOfDay(dateForHandler));

            if (isCurrent) {
                cellBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;");
            } else {
                cellBox.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #999;");
                dayLabel.setStyle("-fx-text-fill: #999;");
            }

            calendarGrid.add(cellBox, col, row);
        }
    }

    private void showTransactionsOfDay(LocalDate date) {
        List<Transaction> list = repo.findByDate(currentUserId, date);
        VBox box = new VBox(6);
        box.setPadding(new Insets(8));
        if (list.isEmpty()) {
            box.getChildren().add(new Label("Không có giao dịch"));
        } else {
            for (Transaction t : list) {
                Label label = new Label(String.format("%s | %s | %,.0f đ | %s", t.getType(), t.getCategory(), Math.abs(t.getAmount()), t.getNote()));
                label.setMaxWidth(Double.MAX_VALUE);
                label.setWrapText(true);
                Button detail = new Button(">");
                detail.setOnAction(e-> openTransactionDetailWindow(t));
                HBox row = new HBox(label, detail);
                HBox.setHgrow(label, Priority.ALWAYS);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setSpacing(8);
                row.setStyle("-fx-padding:6; -fx-background-color: #fff;");
                box.getChildren().add(row);
            }
        }
        transcContainer.setContent(box);
    }

    private void openTransactionDetailWindow(Transaction t){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/project2ver2/transaction_detail.fxml"));
            Scene scene = new Scene(loader.load());
            org.example.project2ver2.controller.TransactionDetailController ctrl = loader.getController();
            ctrl.setTransaction(t);
            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(scene);
            st.setTitle("Chi tiết giao dịch");
            st.showAndWait();
            // refresh after close
            refreshCalendar();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateChartsAndSummary(){
        List<Transaction> all = repo.findAll(currentUserId);
        double income = all.stream().filter(t-> "Thu".equalsIgnoreCase(t.getType())).mapToDouble(Transaction::getAmount).sum();
        double expense = all.stream().filter(t-> "Chi".equalsIgnoreCase(t.getType())).mapToDouble(Transaction::getAmount).sum();
        incomeLabel.setText(String.format("%,.0f", income));
        expenseLabel.setText(String.format("%,.0f", Math.abs(expense)));
        totalLabel.setText(String.format("%,.0f", income + expense));

        // Pie by category (Chi only)
        Map<String, Double> byCat = all.stream()
                .filter(t-> "Chi".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(t->Math.abs(t.getAmount()))));
        pieChart.getData().clear();
        byCat.forEach((k,v)-> pieChart.getData().add(new PieChart.Data(k, v)));

        // Bar chart: total per month (this year)
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Thu/Chi");
        Map<Integer, Double> monthly = all.stream()
                .filter(t-> t.getCreatedAt()!=null && t.getCreatedAt().getYear() == LocalDate.now().getYear())
                .collect(Collectors.groupingBy(t->t.getCreatedAt().getMonthValue(), Collectors.summingDouble(Transaction::getAmount)));
        for (int m=1;m<=12;m++){
            double val = monthly.getOrDefault(m, 0.0);
            series.getData().add(new XYChart.Data<>(String.valueOf(m), val));
        }
        barChart.getData().add(series);
    }

    @FXML private void openCategoryDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/project2ver2/category_dialog.fxml"));
            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(loader.load()));
            st.showAndWait();
            refreshCategories();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
