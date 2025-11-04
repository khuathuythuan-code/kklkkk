package org.example.bai6;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DonHangTableView extends Application {
    TableView table = new TableView<>();
    ObservableList<DonHang> list;
    TextField tfTenHang, tfSoLuong, tfDonGia;
    DonHangRepository donHangRepository = new DonHangRepository();


    @Override
    public void start(Stage stage) throws IOException {
        tfTenHang = new TextField();
        tfDonGia = new TextField();
        tfSoLuong = new TextField();

        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);
        form.add(new Label("ten hang"),0,0);
        form.add(tfTenHang,1,0);
        form.add(new Label("so luong"), 0, 1);
        form.add(tfSoLuong,1,1);
        form.add(new Label("don gia"), 0,2);
        form.add(tfDonGia, 1, 2);

        Button addBtn = new Button("add");
        Button updateBtn = new Button("update");
        Button deleteBtn = new Button("delete");
        addBtn.setOnAction(e-> add());
        updateBtn.setOnAction(e-> update());
        deleteBtn.setOnAction(e->delete());


        VBox btnContainer = new VBox(5, addBtn, updateBtn,deleteBtn);

        HBox formAndBtnContainer = new HBox(50,form,btnContainer);
        formAndBtnContainer.setPadding(new Insets(30));
        list = FXCollections.observableArrayList();
        loadDataForTableView();
        table.setItems(list);
        TableColumn<DonHang, Integer> idCol = new TableColumn<>("ID");
        TableColumn<DonHang, String> nameCol = new TableColumn<>("Ten hang");
        TableColumn<DonHang, Integer> soLuongCol = new TableColumn<>("So Luong");
        TableColumn<DonHang, String> donGiaCol = new TableColumn<>("Don Gia");
        TableColumn<DonHang, String> triGiaCol = new TableColumn<>("Tri Gia");
        TableColumn<DonHang, String> thueCol = new TableColumn<>("Thue");
        TableColumn<DonHang, String> cuocChuyenChoCol = new TableColumn<>("Cuoc chuyen cho");
        TableColumn<DonHang, String> tongCongCol = new TableColumn<>("Tong cong");
        TableColumn<DonHang, String> createdCol = new TableColumn<>("Created At");
        TableColumn<DonHang, String> updatedCol = new TableColumn<>("Updated At");

        idCol.setCellValueFactory(d-> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        nameCol.setCellValueFactory(d-> new SimpleStringProperty(d.getValue().getTenHang()));
        soLuongCol.setCellValueFactory(d-> new SimpleIntegerProperty(d.getValue().getSoLuong()).asObject());
        donGiaCol.setCellValueFactory(d-> new SimpleStringProperty(numberFormat(d.getValue().getDonGia())));
        triGiaCol.setCellValueFactory(d-> new SimpleStringProperty(numberFormat(d.getValue().getSoLuong())));
        thueCol.setCellValueFactory(d-> new SimpleStringProperty(numberFormat(d.getValue().getThue())));
        cuocChuyenChoCol.setCellValueFactory(d->new SimpleStringProperty(numberFormat(d.getValue().getCuocChuyenCho())));
        tongCongCol.setCellValueFactory(d-> new SimpleStringProperty(numberFormat(d.getValue().getTongCong())));
        createdCol.setCellValueFactory(d-> new SimpleStringProperty(dateFormat(d.getValue().getCreatedAt())));
        updatedCol.setCellValueFactory(d-> new SimpleStringProperty(dateFormat(d.getValue().getUpdatedAt())));

        table.getColumns().addAll(idCol,nameCol,soLuongCol,donGiaCol,triGiaCol,thueCol,cuocChuyenChoCol,tongCongCol,createdCol,updatedCol);
        table.getSelectionModel().selectedItemProperty().addListener((observale,oldVal,newVal) ->{
            DonHang d = (DonHang) table.getSelectionModel().getSelectedItem();
            if (d != null){
                tfTenHang.setText(d.getTenHang());
                tfSoLuong.setText(String.valueOf(d.getSoLuong()));
                tfDonGia.setText(String.valueOf(d.getDonGia()));
            }
        });

        VBox root = new VBox(formAndBtnContainer,table);
        Scene scene = new Scene(root, 800, 800);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    private void update() {
        DonHang d = (DonHang) table.getSelectionModel().getSelectedItem();
        if ( d!= null){
            String tenHang = tfTenHang.getText();
            int soLuong = Integer.parseInt(tfSoLuong.getText());
            float donGia = Float.parseFloat(tfDonGia.getText());
            DonHang g = new DonHang(d.getId(), tenHang, soLuong, donGia);
            donHangRepository.update(g);
            loadDataForTableView();
        } else {
            alert("chua chon hang de update kia");
        }

    }



    private void delete() {
        DonHang d = (DonHang) table.getSelectionModel().getSelectedItem();
        if (d!= null){
            donHangRepository.delete(d.getId());
            loadDataForTableView();
        } else {
            alert("chua chon o de xoa kia");
        }
    }

    private void add() {
        String tenHang= tfTenHang.getText();
        int soLuong = Integer.parseInt(tfSoLuong.getText());
        float donGia = Float.parseFloat(tfDonGia.getText());
        DonHang d = new DonHang(tenHang,soLuong,donGia);
        donHangRepository.add(d);
        loadDataForTableView();
    }

    private SimpleDateFormat dateFormatModal = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private String dateFormat(Date date) {
        return dateFormatModal.format(date);
    }

    private NumberFormat numberFormatModel = NumberFormat.getNumberInstance(Locale.US);

    private String numberFormat (float n){
        numberFormatModel.setMaximumFractionDigits(0);
        return numberFormatModel.format(n);
    }


    private void loadDataForTableView() {
        list.clear();
        list.addAll(donHangRepository.findAll());
    }



    private void alert(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setContentText(s);
    }


    public static void main(String[] args) {
        launch();
    }
}