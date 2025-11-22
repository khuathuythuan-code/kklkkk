package com.ExpenseTracker.model;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private int userId;
    private String type; // "Thu" or "Chi"
    private String category;
    private float amount;
    private String note;
    private LocalDateTime createdAt;
    private String transMethod;
    private LocalDateTime updatedAt;

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Transaction(int id, int userId, String type,
                       String category, float amount, String note,
                       LocalDateTime createdAt, String transMethod,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.createdAt = createdAt;
        this.transMethod = transMethod;
        this.updatedAt = updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Transaction() {
    }

    public Transaction(int id, int userId, String type, String category, float amount, String note, LocalDateTime createdAt, String transMethod) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.createdAt = createdAt;
        this.transMethod = transMethod;
    }

    // getters + setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getTransMethod() {
        return transMethod;
    }

    public void setTransMethod(String transMethod) {
        this.transMethod = transMethod;
    }
}
