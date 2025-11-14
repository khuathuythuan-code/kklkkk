package com.ExpenseTracker.model;

public class Transaction {
    private String date, description, income, expense, balance;

    public Transaction(String date, String description, String income, String expense, String balance) {
        this.date = date;
        this.description = description;
        this.income = income;
        this.expense = expense;
        this.balance = balance;
    }

    public String getDate() { return date; }
    public String getDescription() { return description; }
    public String getIncome() { return income; }
    public String getExpense() { return expense; }
    public String getBalance() { return balance; }
}
