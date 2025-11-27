package com.ExpenseTracker.repository;

import com.ExpenseTracker.model.Goal;
import com.ExpenseTracker.utility.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GoalRepository {
    private List<Goal> cache = new ArrayList<>();
    private int cachedUserId = -1;

    /** Lấy tất cả mục tiêu của user từ cache nếu hợp lệ */
    public List<Goal> getGoalsByUserCached(int userId) throws SQLException {
        if (cachedUserId != userId || cache.isEmpty()) {
            cache = getGoalsByUserFromDB(userId);
            cachedUserId = userId;
        }
        return cache;
    }

    /** Lấy trực tiếp từ DB */
    public List<Goal> getGoalsByUserFromDB(int userId) throws SQLException {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) { // conn sẽ tự động đóng
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                goals.add(new Goal(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("goal_name"),
                        rs.getLong("goal_amount"),
                        rs.getLong("spending_limit"),
                        rs.getTimestamp("created_date").toLocalDateTime(),
                        rs.getDate("target_date").toLocalDate(),
                        rs.getLong("progress"),
                        rs.getInt("is_completed"),
                        rs.getTimestamp("completed_date") != null ? rs.getTimestamp("completed_date").toLocalDateTime() : null
                ));
            }
        }
        return goals;
    }


    /** Thêm mục tiêu */
    public void addGoal(Goal goal) throws SQLException {
        String sql = "INSERT INTO goals (user_id, goal_name, goal_amount, spending_limit, target_date, progress, is_completed, completed_date, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, goal.getUserId());
            stmt.setString(2, goal.getGoalName());
            stmt.setLong(3, goal.getGoalAmount());
            stmt.setLong(4, goal.getSpendingLimit());
            stmt.setDate(5, Date.valueOf(goal.getTargetDate()));
            stmt.setLong(6, goal.getProgress());
            stmt.setInt(7, goal.getIsCompleted());
            stmt.setTimestamp(8, goal.getCompletedDate() != null ? Timestamp.valueOf(goal.getCompletedDate()) : null);
            stmt.setTimestamp(9, goal.getCreatedDate() != null ? Timestamp.valueOf(goal.getCreatedDate()) : null);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    goal.setId(rs.getInt(1));
                }
            }
        }

        if (goal.getUserId() == cachedUserId)
            cache.add(goal);
    }



    /** Cập nhật toàn bộ mục tiêu */
    public void updateGoal(Goal goal) throws SQLException {
        String sql = "UPDATE goals SET goal_name=?, goal_amount=?, spending_limit=?, target_date=?, progress=?, is_completed=?, completed_date=?,created_date =? WHERE id=?";

        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, goal.getGoalName());
            stmt.setLong(2, goal.getGoalAmount());
            stmt.setLong(3, goal.getSpendingLimit());
            stmt.setDate(4, Date.valueOf(goal.getTargetDate()));
            stmt.setLong(5, goal.getProgress());
            stmt.setInt(6, goal.getIsCompleted());
            stmt.setTimestamp(7, goal.getCompletedDate() != null ? Timestamp.valueOf(goal.getCompletedDate()) : null);
            stmt.setTimestamp(8, goal.getCompletedDate() != null ? Timestamp.valueOf(goal.getCompletedDate()) : null);
            stmt.setInt(9, goal.getId());
            stmt.executeUpdate();
        }

        // update cache
        cache.replaceAll(g -> g.getId() == goal.getId() ? goal : g);
    }

    /** Cập nhật progress */
    public void updateProgress(int goalId, long progress) throws SQLException {
        String sql = "UPDATE goals SET progress = ? WHERE id = ?";

        try (        Connection conn = DBUtil.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, progress);
            stmt.setInt(2, goalId);
            stmt.executeUpdate();
        }

        cache.stream()
                .filter(g -> g.getId() == goalId)
                .findFirst()
                .ifPresent(g -> g.setProgress(progress));
    }


    /** Đánh dấu hoàn thành */
    public void markGoalCompleted(int id) throws SQLException {
        String sql = "UPDATE goals SET is_completed = 1, completed_date = CURRENT_TIMESTAMP WHERE id = ?";
        try (        Connection conn = DBUtil.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        cache.stream()
                .filter(g -> g.getId() == id)
                .findFirst()
                .ifPresent(g -> {
                    g.setIsCompleted(1);
                    g.setCompletedDate(LocalDateTime.now());

                });
    }


    /** Xóa mục tiêu */
    public void deleteGoal(int goalId) throws SQLException {
        String sql = "DELETE FROM goals WHERE id = ?";

        try (        Connection conn = DBUtil.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, goalId);
            stmt.executeUpdate();
        }

        cache.removeIf(g -> g.getId() == goalId);
    }
}
