package org.example.project2ver2;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionRepository {
    public List<Transaction> findAll(int currentUserId){
        List<Transaction> transactions = new ArrayList<>();
        Connection con = DBUtil.getConnection();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("select * from t_don_hang");
            rs = stm.executeQuery();
            while (rs.next()){
                Transaction d = new Transaction();
                d.setId(rs.getInt("id"));
                d.setUserId(rs.getInt("user_id"));
                d.setType(rs.getString("type"));
                d.setCategory(rs.getString("category"));
                d.setAmount(rs.getFloat("amount"));
                d.setNote(rs.getString("note"));
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                transactions.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.closeAll(con, stm, rs);
        }
        List<Transaction> currentUserTransactions =transactions.stream()
                .filter(t -> t.getUserId() == currentUserId)
                .collect(Collectors.toList());
        return currentUserTransactions;
    }

    public void add(Transaction d) {
        Connection con = DBUtil.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("INSERT INTO transactions " +
                    "(user_id, type, category, amount, note, created_at) "
                    + "values(?, ?, ?, ?, ?, ?)");
            stm.setInt(1, d.getUserId());
            stm.setString(2, d.getType());
            stm.setString(3, d.getCategory());
            stm.setFloat(4, d.getAmount());
            stm.setString(5, d.getNote());
            stm.setTimestamp(6, Timestamp.valueOf(d.getCreatedAt()));

            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(con, stm, null);
        }
    }


    public void update(Transaction d) {
        Connection con = DBUtil.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("update Transactions "
                    + " set user_id=?, type=?, category=?, " +
                    "amount=?, note=?, created_at=? where id = ?");
            stm.setInt(1, d.getUserId());
            stm.setString(2, d.getType());
            stm.setString(3, d.getCategory());
            stm.setFloat(4, d.getAmount());
            stm.setString(5, d.getNote());
            stm.setTimestamp(6, Timestamp.valueOf(d.getCreatedAt()));
            stm.setInt(7, d.getUserId());

            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(con, stm, null);
        }
    }

    public void delete(int id) {
        Connection con = DBUtil.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("delete from transactions where id = ?");

            stm.setInt(1, id);
            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(con, stm, null);
        }
    }

}


