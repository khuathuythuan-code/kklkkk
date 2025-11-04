package org.example.project2ver1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class CalendarController {
    @FXML
    void switchToInputExpense(ActionEvent event) throws Exception {
        App.setRoot("input-expense.fxml");
    }


    @FXML
    void switchToReport(ActionEvent event) throws Exception {
        App.setRoot("report.fxml");
    }

    @FXML
    void switchToOthers(ActionEvent event) throws Exception {
        App.setRoot("others.fxml");
    }
}
