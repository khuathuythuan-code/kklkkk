package org.example.project2ver1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class LoginController {

//    @FXML
//    void switchToInputExpense(ActionEvent event) throws IOException {
//        // 1️⃣ Tải file FXML mới
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("input-expense.fxml"));
//        Parent root = loader.load();
//
//        // 2️⃣ Lấy Stage hiện tại thông qua sự kiện (event)
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//
//        // 3️⃣ Tạo Scene mới từ FXML đã tải
//        Scene scene = new Scene(root);
//
//        // 4️⃣ Gán Scene mới vào Stage
//        stage.setScene(scene);
//        stage.show();
//    }
//
//    @FXML
//    private AnchorPane contentArea;
//
//    @FXML
//    void switchContent(ActionEvent event) throws IOException {
//        Parent newContent = FXMLLoader.load(getClass().getResource("input-expense.fxml"));
//        contentArea.getChildren().setAll(newContent);
//    }


    @FXML
    void switchToInputExpense(ActionEvent event) throws Exception {
        App.setRoot("input-expense.fxml");
    }

    @FXML
    void switchToSignUp(ActionEvent actionEvent) throws Exception {
        App.setRoot("sign-up.fxml");
    }
}