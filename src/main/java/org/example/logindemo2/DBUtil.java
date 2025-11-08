package org.example.logindemo2;

import java.sql.*;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/demo_login";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) try { res.close(); } catch (Exception ignored) {}
        }
    }
}
