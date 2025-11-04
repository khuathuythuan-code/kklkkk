module org.example.bai6 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.bai6 to javafx.fxml;
    exports org.example.bai6;
}