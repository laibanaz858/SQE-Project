package com.yourcompany.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDatabase {
    public static void main(String[] args) {
        System.out.println("=== Testing Database Connection ===");
        
        String url = "jdbc:mysql://localhost:3306/test_automation";
        String user = "root";
        String password = "";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL Driver loaded");
            
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Database Connected: " + url);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM login_test_data");
            
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("Login test data count: " + count);
            }
            
            rs = stmt.executeQuery("SELECT * FROM login_test_data WHERE test_case = 'ValidLogin'");
            if (rs.next()) {
                System.out.println("Sample Data:");
                System.out.println("  Test Case: " + rs.getString("test_case"));
                System.out.println("  Username: " + rs.getString("username"));
                System.out.println("  Password: " + rs.getString("password"));
                System.out.println("  Expected: " + rs.getString("expected_result"));
            }
            
            rs.close();
            stmt.close();
            conn.close();
            System.out.println("Database test completed successfully!");
            
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver not found");
            System.out.println("Add to pom.xml: mysql:mysql-connector-java:8.0.33");
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}