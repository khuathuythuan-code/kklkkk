package com.ExpenseTracker.controller;

import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.TransactionUpdateListener;
import com.ExpenseTracker.model.Transaction;
import com.ExpenseTracker.repository.TransactionRepository;
import com.ExpenseTracker.utility.ChangeSceneUtil;
import com.ExpenseTracker.utility.LanguageManagerUlti;
import com.ExpenseTracker.utility.ThemeUtil;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HistoryController implements Initializable, TransactionUpdateListener {
    private final TransactionRepository repo = new TransactionRepository();
    private final int currentUserId = Singleton.getInstance().currentUser; // demo; thay bằng session User
    private LocalDate currentSelectedDate = LocalDate.now();

    @FXML private TextField searchField;
    @FXML private ScrollPane ScrollContainer;
    @FXML private GridPane calendarGrid;
    @FXML private ComboBox<Integer> monthComboBox, yearComboBox;
    @FXML private javafx.scene.text.Text monthLabel, yearLabel, searchLabel;
    @FXML private Button btnHome, btnHistory, btnReport, btnSettings;
    @FXML private ComboBox<String> cbFilterMode;
    @FXML private ComboBox<Integer> cbMonth;
    @FXML private ComboBox<Integer> cbYear;
    @FXML VBox calendarAndSearchingContainer;

    // transaction UI container (single instance)
    private VBox transactionContainer;
    private Label typeLabel;

    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private List<Transaction> allTransactions = Collections.emptyList();

    // scroll / loading control
    private boolean isLoading = false;
    private boolean infiniteEnabled = true;

    private enum SearchMode { DEFAULT, MONTH, YEAR }
    private SearchMode currentSearchMode = SearchMode.DEFAULT;

    private int selectedMonth;
    private int selectedYear;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Cập nhật locale
        LanguageManagerUlti.setLocale(Singleton.getInstance().currentLanguage);

        // Cập nhật text cho UI
        bindTexts();

        initMonthYear();
        refreshCalendar();
        monthComboBox.setOnAction(e -> refreshCalendar());
        yearComboBox.setOnAction(e -> refreshCalendar());

        PauseTransition pause = new PauseTransition(Duration.millis(300));
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {

            pause.setOnFinished(e -> {
                String keyword = newValue.trim();
                selectedMonth = cbMonth.getValue();
                selectedYear = cbYear.getValue();

                if (keyword.isEmpty()) {
                    // 👇 Khi clear text -> quay lại UI đúng mode hiện tại
                    infiniteEnabled = (currentSearchMode == SearchMode.DEFAULT);

                    switch (currentSearchMode) {
                        case DEFAULT -> loadDefaultTransactions();
                        case MONTH -> loadTransactionsByMonth();
                        case YEAR -> loadTransactionsByYear();
                    }
                    return; // Dừng luôn ở đây
                }

                // 👇 Khi đang nhập -> tìm theo đúng chế độ lọc
                infiniteEnabled = false;
                List<Transaction> result;

                switch (currentSearchMode) {
                    case DEFAULT ->
                            result = repo.searchByKey(currentUserId, keyword);
                    case MONTH ->
                            result = repo.searchByKeyInMonth(currentUserId, keyword, selectedMonth, selectedYear);
                    case YEAR ->
                            result = repo.searchByKeyInYear(currentUserId, keyword, selectedYear);
                    default ->
                            result = Collections.emptyList();
                }

                clearAndRenderFull(result, true);
            });

            pause.playFromStart();
        });

        cbFilterMode.getItems().setAll(
                Singleton.getInstance().currentLanguage.equalsIgnoreCase("en")
                        ? new String[]{"By default", "By month", "By year"}
                        : new String[]{"Mặc định", "Theo tháng", "Theo năm"}
        );

        cbFilterMode.getSelectionModel().selectFirst();

        int currentMonth = LocalDate.now().getMonthValue();
        cbMonth.getItems().addAll(IntStream.rangeClosed(1, 12).boxed().toList());
        cbMonth.getSelectionModel().select(Integer.valueOf(currentMonth));

        int currentYear = LocalDate.now().getYear();
        cbYear.getItems().addAll(IntStream.rangeClosed(currentYear - 10, currentYear).boxed().toList());
        cbYear.getSelectionModel().select(Integer.valueOf(currentYear));

        updateVisibleFilters();

        // init transaction container ONCE
        transactionContainer = new VBox(12);
        transactionContainer.setPadding(new Insets(10));
        transactionContainer.setStyle("-fx-background-color: #f4f6f8;");
        transactionContainer.setFillWidth(true);
        calendarAndSearchingContainer.getChildren().add(transactionContainer);

        // attach scroll listener for infinite load
        setupInfiniteScroll();

        // load default
        loadDefaultTransactions();

        cbFilterMode.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateVisibleFilters();
            refreshTransactionList();
        });

        cbMonth.valueProperty().addListener((o, ov, nv) -> refreshTransactionList());
        cbYear.valueProperty().addListener((o, ov, nv) -> refreshTransactionList());
    }

    private void setupInfiniteScroll() {
        // listen vertical scroll, trigger when near bottom
        ScrollContainer.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (!infiniteEnabled) return;
            if (isLoading) return;

            // threshold: 0.92 -> gần cuối
            if (newVal.doubleValue() >= 0.92) {
                int maxPages = (allTransactions.size() + PAGE_SIZE - 1) / PAGE_SIZE;
                if (currentPage + 1 < maxPages) {
                    isLoading = true;
                    currentPage++;
                    // small delay for smoother UX
                    PauseTransition p = new PauseTransition(Duration.millis(120));
                    p.setOnFinished(ev -> {
                        renderPageAppend();
                        isLoading = false;
                    });
                    p.play();
                }
            }
        });
    }

    private void updateVisibleFilters() {
        String mode = cbFilterMode.getValue();
        boolean findByMonth ;
        boolean findByYear ;
        if (Singleton.getInstance().currentLanguage.equalsIgnoreCase("vi")){
           findByMonth = "Theo tháng".equals(mode);
           findByYear = "Theo năm".equals(mode);
        } else {
            findByMonth = "By month".equals(mode);
            findByYear = "By year".equals(mode);
        }


        cbMonth.setVisible(findByMonth);
        cbYear.setVisible(findByMonth || findByYear);
    }

    private void refreshTransactionList() {
        String mode = cbFilterMode.getValue();
        switch (mode) {
            case "Mặc định", "By default" -> loadDefaultTransactions();
            case "Theo tháng", "By month" -> loadTransactionsByMonth();
            case "Theo năm", "By year" -> loadTransactionsByYear();
        }
    }

    private void loadDefaultTransactions() {
        // phân trang mặc định, enable infinite scroll
        currentSearchMode = SearchMode.DEFAULT;
        infiniteEnabled = true;
        allTransactions = repo.findAllCached(currentUserId).stream()
                .sorted(Comparator.comparing(Transaction::getUpdatedAt).reversed())
                .toList();
        currentPage = 0;
        // reset container then render first page
        transactionContainer.getChildren().clear();
        renderPageAppend(); // render page 0
    }

    /**
     * Render (append) the current page's items into transactionContainer.
     * This tries to preserve grouping by date: if last group has same date, append into it.
     */
    private void renderPageAppend() {
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allTransactions.size());
        if (start >= end) return;

        List<Transaction> pageList = allTransactions.subList(start, end);
        // we want to group the page by date to create grouped blocks per date
        Map<LocalDate, List<Transaction>> grouped =
                pageList.stream()
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

            // check if last child group of transactionContainer has same date
            if (!transactionContainer.getChildren().isEmpty()) {
                Node lastNode = transactionContainer.getChildren().get(transactionContainer.getChildren().size() - 1);
                if (lastNode instanceof VBox) {
                    VBox lastGroup = (VBox) lastNode;
                    if (!lastGroup.getChildren().isEmpty() && lastGroup.getChildren().get(0) instanceof Label) {
                        Label lastDateLabel = (Label) lastGroup.getChildren().get(0);
                        String expected = "📅 " + df.format(date);
                        if (expected.equals(lastDateLabel.getText())) {
                            // append into existing dayBox (assumed 2nd child)
                            if (lastGroup.getChildren().size() >= 2 && lastGroup.getChildren().get(1) instanceof VBox) {
                                VBox existingDayBox = (VBox) lastGroup.getChildren().get(1);
                                for (Transaction t : dayList) {
                                    Node row = buildTransactionRow(t);
                                    applyFadeIn(row);
                                    existingDayBox.getChildren().add(row);
                                }
                                continue; // next date
                            }
                        }
                    }
                }
            }

            // else create a new group for that date
            VBox group = createGroupForDate(date, dayList);
            applyFadeIn(group);
            transactionContainer.getChildren().add(group);
        }

        // ensure content fits width and ScrollContainer uses the transactionContainer
        ScrollContainer.setFitToWidth(true);
        ScrollContainer.setContent(transactionContainer);
    }

    private VBox createGroupForDate(LocalDate date, List<Transaction> dayList) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
            Node row = buildTransactionRow(t);
            applyFadeIn(row);
            dayBox.getChildren().add(row);
        }

        VBox group = new VBox(5, dateLabel, dayBox);
        group.setMaxWidth(Double.MAX_VALUE);
        return group;
    }

    /**
     * Build a single transaction row as Node (replaces inline code).
     */
    private Node buildTransactionRow(Transaction t) {
        boolean isIncome = "Thu".equalsIgnoreCase(t.getType());
        if (Singleton.getInstance().currentLanguage.equalsIgnoreCase("vi")) {
            typeLabel = new Label(isIncome ? "Thu" : "Chi");
        } else {
            typeLabel = new Label(isIncome ? "Income" : "Expense");
        }
        typeLabel.setStyle(String.format("""
                -fx-font-weight: bold;
                -fx-text-fill: %s;
                -fx-font-size: 13px;
                -fx-min-width: 40;
            """, isIncome ? "#145a32" : "#b71c1c"));

        VBox leftBox = new VBox(2);
        Label category = new Label(t.getCategory());
        category.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a1a1a;");

        Label note = new Label(t.getNote() == null ? "" : t.getNote());
        note.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        note.setWrapText(true);

        String paymentMethod = t.getTransMethod() == null || t.getTransMethod().isBlank()
                ? LanguageManagerUlti.get("History.transaction.method.unknown")
                : LanguageManagerUlti.get("History.transaction.method") + t.getTransMethod();
        Label method = new Label(paymentMethod);
        method.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        method.setWrapText(true);

        leftBox.getChildren().addAll(category, note, method);

        Label amount = new Label(String.format("%,.0f đ", Math.abs(t.getAmount())));
        amount.setStyle(String.format("""
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: %s;
            """, isIncome ? "#1e8449" : "#c0392b"));
        amount.setAlignment(Pos.CENTER_RIGHT);

        Button detail = new Button(LanguageManagerUlti.get("History.transaction.detail.button"));
        detail.setOnAction(e -> openTransactionDetailWindow(t));
        detail.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #2980b9;
                -fx-underline: true;
                -fx-cursor: hand;
                -fx-font-size: 12px;
            """);

        VBox rightBox = new VBox(2, amount, detail);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(10, typeLabel, leftBox, rightBox);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 8;
                -fx-padding: 8 12 8 12;
            """, isIncome ? "#d4efdf" : "#f9e6e6"));
        row.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.SOMETIMES);

        // click to open detail
        row.setOnMouseClicked(e -> openTransactionDetailWindow(t));

        return row;
    }

    private void applyFadeIn(Node node) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(260), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * When we are in non-paginated modes (filter/search/show by date), clear and render entire list.
     */
    private void clearAndRenderFull(List<Transaction> list, boolean scrollToTop) {
        infiniteEnabled = false;
        transactionContainer.getChildren().clear();

        if (list == null || list.isEmpty()) {
            Label noData = new Label(LanguageManagerUlti.get("History.transaction.noData"));
            noData.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            noData.setAlignment(Pos.CENTER);
            noData.setMaxWidth(Double.MAX_VALUE);
            transactionContainer.getChildren().add(noData);
        } else {
            Map<LocalDate, List<Transaction>> grouped =
                    list.stream()
                            .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                            .collect(Collectors.groupingBy(
                                    t -> t.getCreatedAt().toLocalDate(),
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));
            for (Map.Entry<LocalDate, List<Transaction>> entry : grouped.entrySet()) {
                VBox group = createGroupForDate(entry.getKey(), entry.getValue());
                transactionContainer.getChildren().add(group);
            }
        }
        ScrollContainer.setFitToWidth(true);
        ScrollContainer.setContent(transactionContainer);
        if (scrollToTop) ScrollContainer.setVvalue(0);
    }



    private void loadTransactionsByMonth() {
        currentSearchMode = SearchMode.MONTH;
        Integer month = cbMonth.getValue();
        Integer year = cbYear.getValue();
        if (month == null || year == null) return;

        List<Transaction> filtered = repo.findByMonth(currentUserId, month, year)
                .stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .toList();
        clearAndRenderFull(filtered, true);
    }

    private void loadTransactionsByYear() {
        currentSearchMode = SearchMode.YEAR;
        Integer year = cbYear.getValue();
        if (year == null) return;

        List<Transaction> filtered = repo.findByYear(currentUserId, year)
                .stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .toList();
        clearAndRenderFull(filtered, true);
    }

    private void initMonthYear(){
        int curYear = LocalDate.now().getYear();
        for (int m=1;m<=12;m++) monthComboBox.getItems().add(m);
        for (int y = curYear-5; y <= curYear+1; y++) yearComboBox.getItems().add(y);
        monthComboBox.setValue(LocalDate.now().getMonthValue());
        yearComboBox.setValue(curYear);
    }

    private void refreshCalendar(){
        int month = monthComboBox.getValue();
        int year = yearComboBox.getValue();
        generateCalendar(month, year);
    }

    private void generateCalendar(int month, int year) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        for (int i = 0; i < 7; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(100.0 / 7);
            calendarGrid.getColumnConstraints().add(c);
        }
        for (int i = 0; i < 7; i++) {
            RowConstraints r = new RowConstraints();
            r.setPercentHeight(100.0 / 7);
            calendarGrid.getRowConstraints().add(r);
        }

        String[] vidays = {"T2","T3","T4","T5","T6","T7","CN"};
        String[] endays = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};

        for (int i = 0; i < 7; i++) {
            Label l = new Label(
                    Singleton.getInstance().currentLanguage.equalsIgnoreCase("vi") ? vidays[i] : endays[i]
            );
            l.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill:#4b0082;");
            l.setAlignment(Pos.CENTER);
            l.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            calendarGrid.add(l, i, 0);
        }

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int firstWeekday = firstDay.getDayOfWeek().getValue(); // 1=Mon
        LocalDate prevMonth = firstDay.minusMonths(1);
        int daysInPrev = prevMonth.lengthOfMonth();
        int daysInMonth = firstDay.lengthOfMonth();

        int startCol = firstWeekday - 1;
        int totalCells = 6 * 7;
        int dayCounterPrevStart = daysInPrev - startCol + 1;

        for (int i = 0; i < totalCells; i++) {
            int row = i / 7 + 1;
            int col = i % 7;

            VBox cellBox = new VBox();
            cellBox.setAlignment(Pos.TOP_CENTER);
            cellBox.setPadding(new Insets(6));
            cellBox.setSpacing(4);
            cellBox.setPrefSize(80, 80);
            cellBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            cellBox.setStyle("-fx-background-radius:12; -fx-border-radius:12; -fx-border-color:transparent;");

            LocalDate date;
            boolean isCurrent;
            if (i < startCol) {
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
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size:14px;");

            if (!isCurrent) {
                cellBox.setStyle("-fx-background-color:#f5f5f5; -fx-background-radius:12;");
                dayLabel.setStyle("-fx-text-fill:#999; -fx-font-weight:normal; -fx-font-size:13px;");
            } else {
                cellBox.setStyle("-fx-background-color:#ffffff; -fx-background-radius:12; -fx-border-color:#e0e0e0; -fx-border-width:1;");
                dayLabel.setStyle("-fx-text-fill:#333; -fx-font-weight:bold; -fx-font-size:14px;");
            }

            if (date.equals(LocalDate.now())) {
                cellBox.setStyle("-fx-background-color:#cdeffd; -fx-border-color:#00aaff; -fx-border-width:2; -fx-background-radius:12;");
            }

            List<Transaction> txs = repo.findByDate(currentUserId, date);
            if (!txs.isEmpty()) {
                HBox indicators = new HBox(4);
                indicators.setAlignment(Pos.CENTER);
                long thu = txs.stream().filter(t -> "Thu".equalsIgnoreCase(t.getType())).count();
                long chi = txs.stream().filter(t -> "Chi".equalsIgnoreCase(t.getType())).count();
                if (thu > 0) {
                    Label thuLabel = new Label("↑" + thu);
                    thuLabel.setStyle("-fx-text-fill:#28a745; -fx-font-size:11px; -fx-font-weight:bold;");
                    indicators.getChildren().add(thuLabel);
                }
                if (chi > 0) {
                    Label chiLabel = new Label("↓" + chi);
                    chiLabel.setStyle("-fx-text-fill:#dc3545; -fx-font-size:11px; -fx-font-weight:bold;");
                    indicators.getChildren().add(chiLabel);
                }
                cellBox.getChildren().add(indicators);
            }

            final LocalDate dateForHandler = date;
            cellBox.setOnMouseClicked(e -> showTransactions(dateForHandler));

            cellBox.setOnMouseEntered(e -> cellBox.setStyle(cellBox.getStyle() + "; -fx-background-color:#e6f7ff; -fx-cursor:hand;"));
            cellBox.setOnMouseExited(e -> {
                if (date.equals(LocalDate.now())) {
                    cellBox.setStyle("-fx-background-color:#cdeffd; -fx-border-color:#00aaff; -fx-border-width:2; -fx-background-radius:12;");
                } else if (isCurrent) {
                    cellBox.setStyle("-fx-background-color:#ffffff; -fx-border-color:#e0e0e0; -fx-border-width:1; -fx-background-radius:12;");
                } else {
                    cellBox.setStyle("-fx-background-color:#f5f5f5; -fx-background-radius:12;");
                }
            });

            cellBox.getChildren().add(dayLabel);
            calendarGrid.add(cellBox, col, row);
        }
    }

    private void showTransactions(LocalDate date) {
        currentSelectedDate = date;
        repo.refreshCache(currentUserId);
        List<Transaction> list = repo.findByDate(currentUserId, date).stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .toList();
        clearAndRenderFull(list, true);
    }

    private void openTransactionDetailWindow(Transaction t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transaction_detail.fxml"));
            Scene scene = new Scene(loader.load());
            ThemeUtil.applyTheme(scene);
            TransactionDetailController ctrl = loader.getController();
            ctrl.setTransaction(t);
            ctrl.setListener(this);

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(scene);
            st.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    @Override
    public void onTransactionUpdated(Transaction transaction) {
        // cập nhật lại danh sách; nếu đang ở chế độ mặc định thì refresh data source and re-render pages
        if ("Mặc định".equals(cbFilterMode.getValue()) || "By default".equals(cbFilterMode.getValue()) ) {
            loadDefaultTransactions();
        } else {
            showTransactions(transaction.getCreatedAt().toLocalDate());
        }
    }

    @Override
    public void onTransactionDeleted(Transaction transaction) {
        if ("Mặc định".equals(cbFilterMode.getValue()) || "By default".equals(cbFilterMode.getValue())) {
            loadDefaultTransactions();
        } else {
            showTransactions(transaction.getCreatedAt().toLocalDate());
        }
    }

    private void bindTexts() {
        monthLabel.setText(LanguageManagerUlti.get("History.text.month.label"));
        yearLabel.setText(LanguageManagerUlti.get("History.text.year.label"));
        searchLabel.setText(LanguageManagerUlti.get("History.text.search.label"));

        btnHome.setText(LanguageManagerUlti.get("History.button.menu.home"));
        btnHistory.setText(LanguageManagerUlti.get("History.button.menu.history"));
        btnReport.setText(LanguageManagerUlti.get("History.button.menu.report"));
        btnSettings.setText(LanguageManagerUlti.get("History.button.menu.settings"));
    }
}
