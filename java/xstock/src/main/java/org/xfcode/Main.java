package org.xfcode;

import java.sql.*;

public class Main {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/stock";
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,"root", "");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("show tables;");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
          try {
              if (stmt != null) { stmt.close(); }
              if (conn != null) { conn.close(); }
          } catch (SQLException e) {
              throw new RuntimeException(e);
          }
        }
    }

}