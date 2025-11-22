package com.ExpenseTracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class CalculatorPopupController {

    @FXML
    private TextField display;

    private ScriptEngine engine;
    private boolean calculated = false; // đánh dấu vừa tính xong

    @FXML
    public void initialize() {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("JavaScript");
        if (engine == null) {
            System.err.println("Không tìm thấy JavaScript engine!");
        }

        // cho phép nhận phím bàn phím
        display.setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent event) {
        String code = event.getCode().toString();
        switch (code) {
            case "ENTER" -> calculate();
            case "BACK_SPACE" -> backspace();
            default -> {
                String text = event.getText();
                if (text.matches("[0-9]")) appendNumber(text);
                else if (text.matches("[+\\-*/]")) appendOperator(text);
                else if (text.equals(".")) appendDot();
            }
        }
    }

    @FXML
    private void calculate() {
        try {
            String expr = display.getText();
            double result = evalSimple(expr);
            // hiển thị đẹp: bỏ .0 nếu nguyên
            if (result == (long) result) {
                display.setText(String.valueOf((long) result));
            } else {
                display.setText(String.valueOf(result));
            }
            calculated = true;
        } catch (Exception e) {
            display.setText("Error");
            calculated = true;
        }
    }

    private double evalSimple(String expr) throws Exception {
        String replaced = expr.replaceAll("[^0-9+\\-*/.]", "");
        if (engine != null) {
            return ((Number) engine.eval(replaced)).doubleValue();
        } else {
            throw new Exception("No JS engine");
        }
    }

    private void appendNumber(String value) {
        if (calculated) {
            display.clear();
            calculated = false;
        }
        display.appendText(value);
    }

    @FXML
    private void appendNumber(javafx.event.ActionEvent event) {
        String value = ((Button) event.getSource()).getText();
        appendNumber(value);
    }

    private void appendOperator(String value) {
        if (calculated) calculated = false; // cho phép nối tiếp sau khi tính
        String text = display.getText();
        if (text.isEmpty()) return; // không cho phép bắt đầu bằng toán tử
        char last = text.charAt(text.length() - 1);
        if ("+-*/".indexOf(last) != -1) {
            // thay toán tử nếu liên tiếp
            display.setText(text.substring(0, text.length() - 1) + value);
        } else {
            display.appendText(value);
        }
    }

    @FXML
    private void appendOperator(javafx.event.ActionEvent event) {
        String value = ((Button) event.getSource()).getText();
        appendOperator(value);
    }

    private void appendDot() {
        if (calculated) {
            display.clear();
            calculated = false;
        }
        // kiểm tra nếu đã có dấu chấm ở số hiện tại
        String text = display.getText();
        String[] tokens = text.split("[+\\-*/]");
        String last = tokens[tokens.length - 1];
        if (!last.contains(".")) display.appendText(".");
    }

    @FXML
    private void appendDot(javafx.event.ActionEvent event) {
        appendDot();
    }

    @FXML
    private void clearDisplay(javafx.event.ActionEvent event) {
        display.clear();
        calculated = false;
    }

    private void backspace() {
        String text = display.getText();
        if (!text.isEmpty()) display.setText(text.substring(0, text.length() - 1));
    }
}
