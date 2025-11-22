package com.ExpenseTracker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Goal {
    private int id;
    private int userId;
    private String goalName;
    private long goalAmount;
    private long spendingLimit;
    private LocalDateTime createdDate;
    private LocalDate targetDate;
    private long progress;
    private int isCompleted;
    private LocalDateTime completedDate;  // Ngày hoàn thành



    public Goal(int id, int userId, String goalName, long goalAmount, long spendingLimit,
                LocalDateTime createdDate, LocalDate targetDate, long progress, int isCompleted, LocalDateTime completedDate) {
        this.id = id;
        this.userId = userId;
        this.goalName = goalName;
        this.goalAmount = goalAmount;
        this.spendingLimit = spendingLimit;
        this.createdDate = createdDate;
        this.targetDate = targetDate;
        this.progress = progress;
        this.isCompleted = isCompleted;
        this.completedDate = completedDate;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }

    public long getGoalAmount() { return goalAmount; }
    public void setGoalAmount(long goalAmount) { this.goalAmount = goalAmount; }

    public long getSpendingLimit() { return spendingLimit; }
    public void setSpendingLimit(long spendingLimit) { this.spendingLimit = spendingLimit; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public long getProgress() { return progress; }
    public void setProgress(long progress) { this.progress = progress; }

    public int getIsCompleted() { return isCompleted; }
    public void setIsCompleted(int completed) { this.isCompleted = completed; }
    public boolean isCompleted() { return isCompleted == 1; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
}
