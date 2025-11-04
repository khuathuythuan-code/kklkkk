package org.example.project2ver2;

import java.sql.*;

public class dbUtil {

        public static void closeAll(Connection con, PreparedStatement stm, ResultSet rs){
            if (con != null){
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stm != null){
                try {
                    stm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


        public static Connection getConnection(){
            try {
                return DriverManager.getConnection("jdbc:mysql://localhost:3306/qldonhang","root","");
            } catch (SQLException e){
                e.printStackTrace();
                return null;
            }

        }

}
