package com.ExpenseTracker.controller;

import com.ExpenseTracker.Singleton;
import com.ExpenseTracker.TransactionUpdateListener;
import com.ExpenseTracker.model.Transaction;
import com.ExpenseTracker.repository.CategoryRepository;
import com.ExpenseTracker.repository.TransactionRepository;
import com.ExpenseTracker.utility.ChangeSceneUtil;
import com.ExpenseTracker.utility.LanguageManagerUlti;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HistoryController implements Initializable, TransactionUpdateListener {
    private final TransactionRepository repo = new TransactionRepository();
    private final int currentUserId = Singleton.getInstance().currentUser; // demo; thay bằng session User
    private LocalDate currentSelectedDate = LocalDate.now();

    @FXML private TextField searchField;
    @FXML private ScrollPane transcContainer;
    @FXML private GridPane calendarGrid;
    @FXML private ComboBox<Integer> monthComboBox, yearComboBox;
    @FXML private Text monthLabel, yearLabel,searchLabel;
    @FXML private Button btnHome,btnHistory, btnReport,btnSettings;
    Label typeLabel;
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
                List<Transaction> result;
                String keyword = newValue.trim();

                if (keyword.isEmpty()) {
                    // Nếu người dùng xóa hết chữ → trở lại hiển thị theo ngày đang chọn
                    result = repo.findByDate(currentUserId, currentSelectedDate);
                } else {
                    result = repo.searchByKey(currentUserId, keyword);
                }

                renderTransactionList(result, true);
            });
            pause.playFromStart();
        });
    }

    private void initMonthYear(){
        // init month/year
        int curYear = LocalDate.now().getYear();
        for (int m=1;m<=12;m++) {
            monthComboBox.getItems().add(m);
        }
        for (int y = curYear-5; y <= curYear+1; y++) {
            yearComboBox.getItems().add(y);
        }
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

        // 7 cột đều nhau
        for (int i = 0; i < 7; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(100.0 / 7);
            calendarGrid.getColumnConstraints().add(c);
        }

        // 7 hàng (1 hàng header + 6 hàng ngày)
        for (int i = 0; i < 7; i++) {
            RowConstraints r = new RowConstraints();
            r.setPercentHeight(100.0 / 7);
            calendarGrid.getRowConstraints().add(r);
        }

        // Header weekday labels
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
        int totalCells = 6 * 7; // 6 hàng ngày

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

            // Transaction indicators
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

            // Hover effect
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
        List<Transaction> list = repo.findByDate(currentUserId, date);
        renderTransactionList(list, true);
    }


    private void renderTransactionList(List<Transaction> list, boolean scrollToTop) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #f4f6f8;");
        box.setFillWidth(true);

        if (list.isEmpty()) {
            Label noData = new Label(LanguageManagerUlti.get("History.transaction.noData"));
            noData.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            noData.setAlignment(Pos.CENTER);
            noData.setMaxWidth(Double.MAX_VALUE);
            box.getChildren().add(noData);
        } else {
            Map<LocalDate, List<Transaction>> grouped =
                    list.stream()
                            .sorted(Comparator.comparing(Transaction::getUpdatedAt).reversed())
                            .collect(Collectors.groupingBy(
                                    t -> t.getCreatedAt().toLocalDate(),
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Map.Entry<LocalDate, List<Transaction>> entry : grouped.entrySet()) {
                LocalDate date = entry.getKey();
                List<Transaction> dayList = entry.getValue();

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
                    if (Singleton.getInstance().currentLanguage.equalsIgnoreCase("vi")){
                        typeLabel = new Label(isIncome ? "Thu" : "Chi");
                    }else {
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

                    dayBox.getChildren().add(row);
                }

                VBox group = new VBox(5, dateLabel, dayBox);
                group.setMaxWidth(Double.MAX_VALUE);
                box.getChildren().add(group);
            }
        }

        transcContainer.setFitToWidth(true);
        transcContainer.setContent(box);

        // 🔹 Cuộn lên đầu nếu được yêu cầu
        if (scrollToTop) {
            transcContainer.setVvalue(0);
        }
    }



    private void openTransactionDetailWindow(Transaction t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transaction_detail.fxml"));
            Scene scene = new Scene(loader.load());
            TransactionDetailController ctrl = loader.getController();
            ctrl.setTransaction(t);

            // 🟢 Gán callback
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
        // cập nhật lại danh sách, ví dụ:
        showTransactions(transaction.getCreatedAt().toLocalDate());
    }

    @Override
    public void onTransactionDeleted(Transaction transaction) {
        // cập nhật lại danh sách
        showTransactions(transaction.getCreatedAt().toLocalDate());
    }

    private void bindTexts() {
        // Top
        monthLabel.setText(LanguageManagerUlti.get("History.text.month.label"));
        yearLabel.setText(LanguageManagerUlti.get("History.text.year.label"));

        searchLabel.setText(LanguageManagerUlti.get("History.text.search.label"));

        // Bottom Menu
        btnHome.setText(LanguageManagerUlti.get("History.button.menu.home"));
        btnHistory.setText(LanguageManagerUlti.get("History.button.menu.history"));
        btnReport.setText(LanguageManagerUlti.get("History.button.menu.report"));
        btnSettings.setText(LanguageManagerUlti.get("History.button.menu.settings"));
    }


}
