package org.example.project2ver2.controller;

import javafx.animation.PauseTransition;

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
import javafx.util.Duration;
import org.example.project2ver2.model.Transaction;
import org.example.project2ver2.repository.TransactionRepository;
import org.example.project2ver2.repository.CategoryRepository;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AppController implements Initializable, TransactionUpdateListener  {
    private final TransactionRepository repo = new TransactionRepository();
    private final CategoryRepository catRepo = new CategoryRepository();
    private final int currentUserId = 1; // demo; thay bằng session user
    private LocalDate currentSelectedDate = LocalDate.now();

    @FXML private TextField inputMoneyField;
    @FXML private TextField searchField;

    @FXML private DatePicker inputDateField;
    @FXML private TextArea inputNoteField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private Button expenseBtn;
    @FXML private Button statsBtn;
    @FXML private Button inputDataBtn;


    @FXML private ComboBox<Integer> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox, statsMonthComboBox, statsYearComboBox;

    @FXML private GridPane calendarGrid;
    @FXML private ScrollPane transcContainer;
    @FXML private Label incomeLabel, expenseLabel, totalLabel;

    @FXML private PieChart expensePieChart;
    @FXML private PieChart revenuePieChart;
    @FXML private javafx.scene.chart.BarChart<String, Number> barChart;

    private boolean isExpense = true;
    private boolean isMonthlyChart = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // init categories
        refreshCategories();

        // init date fields
        inputDateField.setValue(LocalDate.now());
        inputMoneyField.setPromptText("VD: 150000");

        // init month/year
        int curYear = LocalDate.now().getYear();
        for (int m=1;m<=12;m++) {
            monthComboBox.getItems().add(m);
            statsMonthComboBox.getItems().add(m);
        }
        for (int y = curYear-5; y <= curYear+1; y++) {
            yearComboBox.getItems().add(y);
            statsYearComboBox.getItems().add(y);
        }
        monthComboBox.setValue(LocalDate.now().getMonthValue());
        yearComboBox.setValue(curYear);

        statsMonthComboBox.setValue(LocalDate.now().getMonthValue());
        statsYearComboBox.setValue(curYear);

        inputDataBtn.setOnAction(e-> createTransaction());


        monthComboBox.setOnAction(e -> {
            monthlySummary();
            refreshCalendar();
        });
        yearComboBox.setOnAction(e -> {
            monthlySummary();
            refreshCalendar();
        });


        statsYearComboBox.setOnAction(e-> updateCharts());
        statsMonthComboBox.setOnAction(e-> updateCharts());

        barChart.setVisible(false);
        statsBtn.setOnAction(e-> toggleMonthYearInStats());

        monthlySummary();
        refreshCalendar();
        updateCharts();

        PauseTransition pause = new PauseTransition(Duration.millis(300));
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            pause.setOnFinished(e -> {
                List<Transaction> result;
                String keyword = newValue.trim();

                if (keyword.isEmpty()) {
                    // Nếu người dùng xóa hết chữ → trở lại hiển thị theo ngày đang chọn
                    result = repo.findByDate(currentUserId, currentSelectedDate);
                } else {
                    result = repo.searchByKey(currentUserId, keyword);
                }

                showSearchResults(result);
            });
            pause.playFromStart();
        });


    }


    private void refreshCategories(){
        String typeCate = expenseBtn.getText();
        categoryCombo.getItems().clear();
        List<String> cats = catRepo.findAll(currentUserId, typeCate);
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
        refreshCategories();
    }

    @FXML
    private void toggleMonthYearInStats(){
        isMonthlyChart = !isMonthlyChart;
        statsBtn.setText(isMonthlyChart ? "Hàng tháng" : "Hàng năm");
        barChart.setVisible(!isMonthlyChart);
        statsMonthComboBox.setDisable(!isMonthlyChart);

        updateCharts();
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

            // ✅ chờ DB ghi xong rồi mới cập nhật
            clearInput();
            monthComboBox.setValue(ld.getMonthValue());
            yearComboBox.setValue(ld.getYear());
            refreshCalendar(); // vẽ lại calendar
            updateCharts(); // cập nhật biểu đồ
            showTransactions(ld); // ✅ cập nhật danh sách giao dịch trong ngày
            monthlySummary();

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
        updateCharts();
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
            cellBox.setOnMouseClicked(e -> showTransactions(dateForHandler));

            if (isCurrent) {
                cellBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;");
            } else {
                cellBox.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #999;");
                dayLabel.setStyle("-fx-text-fill: #999;");
            }

            if (date.equals(LocalDate.now())) {
                cellBox.setStyle("-fx-background-color: #cdeffd; -fx-border-color: #00aaff;");
            }

            cellBox.setOnMouseEntered(e -> cellBox.setStyle("-fx-background-color: #e6f7ff;"));
            cellBox.setOnMouseExited(e -> {
                if (isCurrent) {
                    cellBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;");
                }
                else {
                    cellBox.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #999;");
                }
                if (date.equals(LocalDate.now())) {
                    cellBox.setStyle("-fx-background-color: #cdeffd; -fx-border-color: #00aaff;");
                }
            });

            calendarGrid.add(cellBox, col, row);
        }
    }



    private void showTransactions(LocalDate date) {
        currentSelectedDate = date;
        List<Transaction> list = repo.findByDate(currentUserId, date);
        renderTransactionList(list);
    }

    private void showSearchResults(List<Transaction> list) {
        renderTransactionList(list);
    }


    private void renderTransactionList(List<Transaction> list) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #f4f6f8;");
        box.setFillWidth(true);

        if (list.isEmpty()) {
            Label noData = new Label("Không có giao dịch nào.");
            noData.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            noData.setAlignment(Pos.CENTER);
            noData.setMaxWidth(Double.MAX_VALUE);
            box.getChildren().add(noData);
        } else {
            // 🔽 Gom theo ngày (mới nhất trước)
            Map<LocalDate, List<Transaction>> grouped =
                    list.stream()
                            .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                            .collect(Collectors.groupingBy(
                                    t -> t.getCreatedAt().toLocalDate(),
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Map.Entry<LocalDate, List<Transaction>> entry : grouped.entrySet()) {
                LocalDate date = entry.getKey();
                List<Transaction> dayList = entry.getValue();

                // 📅 Nhãn ngày
                Label dateLabel = new Label("📅 " + df.format(date));
                dateLabel.setStyle("""
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                -fx-background-color: #dfe6e9;
                -fx-text-fill: #2d3436;
                -fx-padding: 6 10 6 10;
                -fx-background-radius: 6;
            """);
                dateLabel.setMaxWidth(Double.MAX_VALUE);
                dateLabel.setAlignment(Pos.CENTER_LEFT);

                VBox dayBox = new VBox(6);
                dayBox.setFillWidth(true);

                for (Transaction t : dayList) {
                    boolean isIncome = "Thu".equalsIgnoreCase(t.getType());

                    // 🟢 Loại giao dịch
                    Label typeLabel = new Label(isIncome ? "Thu" : "Chi");
                    typeLabel.setStyle(String.format("""
                        -fx-font-weight: bold;
                        -fx-text-fill: %s;
                        -fx-font-size: 13px;
                        -fx-min-width: 40;
                    """, isIncome ? "#27ae60" : "#c0392b"));

                    // 📁 Danh mục + ghi chú (bên trái)
                    VBox leftBox = new VBox(2);
                    Label category = new Label(t.getCategory());
                    category.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    Label note = new Label(t.getNote() == null ? "" : t.getNote());
                    note.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                    note.setWrapText(true);

                    leftBox.getChildren().addAll(category, note);

                    // 💰 Số tiền (bên phải)
                    Label amount = new Label(String.format("%,.0f đ", Math.abs(t.getAmount())));
                    amount.setStyle(String.format("""
                        -fx-font-size: 14px;
                        -fx-font-weight: bold;
                        -fx-text-fill: %s;
                    """, isIncome ? "#2ecc71" : "#e74c3c"));
                    amount.setAlignment(Pos.CENTER_RIGHT);

                    // 🔘 Nút xem chi tiết
                    Button detail = new Button("Chi tiết");
                    detail.setOnAction(e -> openTransactionDetailWindow(t));
                    detail.setStyle("""
                        -fx-background-color: transparent;
                        -fx-text-fill: #3498db;
                        -fx-underline: true;
                        -fx-cursor: hand;
                        -fx-font-size: 12px;
                    """);

                    // 🔹 HBox chứa số tiền + nút
                    VBox rightBox = new VBox(2, amount, detail);
                    rightBox.setAlignment(Pos.CENTER_RIGHT);

                    // 🧱 HBox chính của dòng giao dịch
                    HBox row = new HBox(10, typeLabel, leftBox, rightBox);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle(String.format("""
                        -fx-background-color: %s;
                        -fx-background-radius: 8;
                        -fx-padding: 8 12 8 12;
                    """, isIncome ? "#ecf9f1" : "#fdecea"));
                    row.setMaxWidth(Double.MAX_VALUE);

                    // ⚖️ Phân bố không gian linh hoạt
                    HBox.setHgrow(leftBox, Priority.ALWAYS);
                    HBox.setHgrow(rightBox, Priority.SOMETIMES);

                    dayBox.getChildren().add(row);
                }

                VBox group = new VBox(5, dateLabel, dayBox);
                group.setMaxWidth(Double.MAX_VALUE);
                box.getChildren().add(group);
            }
        }
        transcContainer.setFitToWidth(true);
        transcContainer.setContent(box);
    }






    private void openTransactionDetailWindow(Transaction t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/project2ver2/transaction_detail.fxml"));
            Scene scene = new Scene(loader.load());
            TransactionDetailController ctrl = loader.getController();
            ctrl.setTransaction(t);

            // 🟢 Gán callback
            ctrl.setListener(this);

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(scene);
            st.setTitle("Chi tiết giao dịch");
            st.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 🟢 Callback thực thi khi con cập nhật hoặc xóa
    @Override
    public void onTransactionUpdated(Transaction transaction) {
        refreshCalendar();
        updateCharts();
        monthlySummary();
        showTransactions(currentSelectedDate);
    }

    @Override
    public void onTransactionDeleted(Transaction transaction) {
        refreshCalendar();
        updateCharts();
        monthlySummary();
        showTransactions(currentSelectedDate);
    }



    private void monthlySummary(){
        List<Transaction> all = repo.findByMonth(currentUserId,monthComboBox.getValue(),yearComboBox.getValue());

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


    private void updateCharts() {

        List<Transaction> annualData = repo.findByYear(currentUserId,statsYearComboBox.getValue());
        List<Transaction> monthlyData = repo.findByMonth(currentUserId, statsMonthComboBox.getValue(), statsYearComboBox.getValue());



        Map<String, Double> revenueCate = (isMonthlyChart?monthlyData: annualData).stream()
                .filter(t -> "Thu".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));

        revenuePieChart.getData().clear();
        revenueCate.forEach((k, v) -> revenuePieChart.getData().add(new PieChart.Data(k, v)));



        // ==== 2. Biểu đồ tròn (PieChart) theo danh mục Chi ====
        Map<String, Double> expenseCate = (isMonthlyChart?monthlyData: annualData).stream()
                .filter(t -> "Chi".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));

        expensePieChart.getData().clear();
        expenseCate.forEach((k, v) -> expensePieChart.getData().add(new PieChart.Data(k, v)));



        // ==== 3. Biểu đồ cột (BarChart) Thu & Chi theo tháng ====
        barChart.getData().clear();

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Thu");

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Chi");

        // Lọc dữ liệu của năm hiện tại
//        int currentYear = LocalDate.now().getYear();
//        List<Transaction> yearData = annualData.stream()
//                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().getYear() == currentYear)
//                .toList();

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
