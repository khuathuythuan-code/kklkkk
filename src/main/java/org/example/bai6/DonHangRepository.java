package org.example.bai6;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DonHangRepository {
    public List<DonHang> findAll(){
        List<DonHang> DonHangs = new ArrayList<>();
        Connection con = Database.getConnection();
        PreparedStatement stm = null;
        ResultSet rs = null;

        try {
            stm = con.prepareStatement("select * from t_don_hang");
            rs = stm.executeQuery();
            while (rs.next()){
                DonHang d = new DonHang();
                d.setId(rs.getInt("id"));
                d.setTenHang(rs.getString("ten_hang"));
                d.setSoLuong(rs.getInt("so_luong"));
                d.setDonGia(rs.getFloat("don_gia"));
                d.setTriGia(rs.getFloat("tri_gia"));
                d.setThue(rs.getFloat("thue"));
                d.setCuocChuyenCho(rs.getFloat("cuoc_chuyen_cho"));
                d.setTongCong(rs.getFloat("tong_cong"));
                d.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                d.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                DonHangs.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            Database.closeAll(con, stm, rs);
        }
        return DonHangs;
    }

    public void add(DonHang d) {
        Connection con = Database.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("INSERT INTO t_don_hang (ten_hang, so_luong, don_gia, tri_gia, thue, cuoc_chuyen_cho, tong_cong, created_at, updated_at) "
                    + "values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stm.setString(1, d.getTenHang());
            stm.setInt(2, d.getSoLuong());
            stm.setFloat(3, d.getDonGia());
            stm.setFloat(4, d.getTriGia());
            stm.setFloat(5, d.getThue());
            stm.setFloat(6, d.getCuocChuyenCho());
            stm.setFloat(7, d.getTongCong());
            stm.setTimestamp(8, new java.sql.Timestamp(d.getCreatedAt().getTime()));
            stm.setTimestamp(9, new java.sql.Timestamp(d.getUpdatedAt().getTime()));
            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Database.closeAll(con, stm, null);
        }
    }

    public void update(DonHang d) {
        Connection con = Database.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("update t_don_hang "
                    + " set ten_hang=?, so_luong=?, don_gia=?, tri_gia=?, thue=?, "
                    + " cuoc_chuyen_cho=?, tong_cong=?, updated_at=? where id=?");
            stm.setString(1, d.getTenHang());
            stm.setInt(2, d.getSoLuong());
            stm.setFloat(3, d.getDonGia());
            stm.setFloat(4, d.getTriGia());
            stm.setFloat(5, d.getThue());
            stm.setFloat(6, d.getCuocChuyenCho());
            stm.setFloat(7, d.getTongCong());
            stm.setDate(8, new java.sql.Date(d.getUpdatedAt().getTime()));
            stm.setInt(9, d.getId());
            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Database.closeAll(con, stm, null);
        }
    }

    public void delete(int id) {
        Connection con = Database.getConnection();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("delete from t_don_hang where id = ?");

            stm.setInt(1, id);
            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Database.closeAll(con, stm, null);
        }
    }
}





