package org.example.project2ver2.repository;

import org.example.project2ver2.DBUtil;
import org.example.project2ver2.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionRepository {
    private List<Transaction> cache = new ArrayList<>();
    private int cachedUserId = -1;

    public List<Transaction> findAllCached(int userId) {
        if (cachedUserId != userId || cache.isEmpty()) {
            cache = findAll(userId);
            cachedUserId = userId;
        }
        return cache;
    }

    public void refreshCache(int userId) {
        cache = findAll(userId);
        cachedUserId = userId;
    }


    public List<Transaction> findAll(int userId) {
        List<Transaction> list = new ArrayList<>();
        Connection con = DBUtil.getConnection();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT * FROM transactions WHERE user_id = ? ORDER BY created_at DESC");
            stm.setInt(1, userId);
            rs = stm.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setUserId(rs.getInt("user_id"));
                t.setType(rs.getString("type"));
                t.setCategory(rs.getString("category"));
                t.setAmount(rs.getFloat("amount"));
                t.setNote(rs.getString("note"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(con, stm, rs);
        }
        return list;
    }

    public List<Transaction> findByYear(int userId, int year) {
        return findAllCached(userId).stream()
                .filter(t -> t.getCreatedAt() != null
                        && t.getCreatedAt().getYear() == year)
                .collect(Collectors.toList());
    }


    public List<Transaction> findByMonth(int userId, int month, int year) {
        return findAllCached(userId).stream()
                .filter(t -> t.getCreatedAt() != null
                        && t.getCreatedAt().getMonthValue() == month
                        && t.getCreatedAt().getYear() == year)
                .collect(Collectors.toList());
    }

    public List<Transaction> findByDate(int userId, java.time.LocalDate date) {
        return findAllCached(userId).stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }


    public List<Transaction> searchByKey(int userId, String keyword) {
        String sql = "SELECT * FROM transactions " +
                "WHERE user_id = ? " +
                "AND (LOWER(category) LIKE ? OR LOWER(note) LIKE ?)";

        List<Transaction> list = new ArrayList<>();
        String k = (keyword == null) ? "" : keyword.trim().toLowerCase();

        // Nếu rỗng -> không tìm
        if (k.isEmpty()) {
            return Collections.emptyList();
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            String pattern = "%" + k + "%";
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setUserId(rs.getInt("user_id"));
                t.setCategory(rs.getString("category"));
                t.setNote(rs.getString("note"));
                t.setAmount(rs.getFloat("amount"));
                t.setType(rs.getString("type"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public void add(Transaction t) {
        Connection con = DBUtil.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("INSERT INTO transactions (user_id,type,category,amount,note,created_at) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            stm.setInt(1, t.getUserId());
            stm.setString(2, t.getType());
            stm.setString(3, t.getCategory());
            stm.setFloat(4, t.getAmount());
            stm.setString(5, t.getNote());
            stm.setTimestamp(6, Timestamp.valueOf(t.getCreatedAt()));
            stm.executeUpdate();
            ResultSet rs = stm.getGeneratedKeys();
            if (rs.next()) t.setId(rs.getInt(1));
            rs.close();
            refreshCache(t.getUserId());
        } catch (SQLException e) { e.printStackTrace();
        } finally { DBUtil.closeAll(con, stm, null); }
    }

    public void update(Transaction t) {
        Connection con = DBUtil.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("UPDATE transactions SET type=?, category=?, amount=?, note=?, created_at=? WHERE id=? AND user_id = ?");
            stm.setString(1, t.getType());
            stm.setString(2, t.getCategory());
            stm.setFloat(3, t.getAmount());
            stm.setString(4, t.getNote());
            stm.setTimestamp(5, Timestamp.valueOf(t.getCreatedAt()));
            stm.setInt(6, t.getId());
            stm.setInt(7, t.getUserId());
            stm.executeUpdate();
            refreshCache(t.getUserId());
        } catch (SQLException e) { e.printStackTrace();
        } finally { DBUtil.closeAll(con, stm, null); }
    }

    public void delete(int id, int userId) {
        Connection con = DBUtil.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("DELETE FROM transactions WHERE id=? AND userID = ?");
            stm.setInt(1, id);
            stm.setInt(2, userId);
            stm.executeUpdate();
            refreshCache(userId);
        } catch (SQLException e) { e.printStackTrace();
        } finally { DBUtil.closeAll(con, stm, null); }
    }




}
