package org.example.project2ver2.repository;

import org.example.project2ver2.DBUtil;
import org.example.project2ver2.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionRepository {

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

    public List<Transaction> findByMonth(int userId, int month, int year) {
        return findAll(userId).stream()
                .filter(t -> t.getCreatedAt() != null
                        && t.getCreatedAt().getMonthValue() == month
                        && t.getCreatedAt().getYear() == year)
                .collect(Collectors.toList());
    }

    public List<Transaction> findByDate(int userId, java.time.LocalDate date) {
        return findAll(userId).stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Transaction> search(int userId, String keyword) {
        String k = keyword == null ? "" : keyword.toLowerCase();
        return findAll(userId).stream()
                .filter(t -> (t.getCategory()!=null && t.getCategory().toLowerCase().contains(k)) ||
                        (t.getNote()!=null && t.getNote().toLowerCase().contains(k)))
                .collect(Collectors.toList());
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
        } catch (SQLException e) { e.printStackTrace();
        } finally { DBUtil.closeAll(con, stm, null); }
    }

    public void update(Transaction t) {
        Connection con = DBUtil.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("UPDATE transactions SET user_id=?, type=?, category=?, amount=?, note=?, created_at=? WHERE id=?");
            stm.setInt(1, t.getUserId());
            stm.setString(2, t.getType());
            stm.setString(3, t.getCategory());
            stm.setFloat(4, t.getAmount());
            stm.setString(5, t.getNote());
            stm.setTimestamp(6, Timestamp.valueOf(t.getCreatedAt()));
            stm.setInt(7, t.getId());
            stm.executeUpdate();
        } catch (SQLException e) { e.printStackTrace();
        } finally { DBUtil.closeAll(con, stm, null); }
    }

    public void delete(int id) {
        Connection con = DBUtil.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("DELETE FROM transactions WHERE id=?");
            stm.setInt(1, id);
            stm.executeUpdate();
        } catch (SQLException e) { e.printStackTrace();
        } finally { DBUtil.closeAll(con, stm, null); }
    }
}
