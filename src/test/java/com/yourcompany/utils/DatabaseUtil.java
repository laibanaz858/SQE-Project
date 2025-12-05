package com.yourcompany.utils;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUtil {
    private static Connection connection;
    private static boolean useMock = false;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/saucedemo_test";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private static final Map<String, Map<String, Map<String, String>>> mockDatabase = new HashMap<>();
    
    static {
        Map<String, Map<String, String>> mockData = new HashMap<>();
        
        Map<String, String> adminUser = new HashMap<>();
        adminUser.put("username", "admin_user");
        adminUser.put("password", "admin_pass");
        adminUser.put("role", "admin");
        mockData.put("AdminUser", adminUser);
        
        Map<String, String> standardUser = new HashMap<>();
        standardUser.put("username", "standard_user");
        standardUser.put("password", "secret_sauce");
        standardUser.put("role", "standard");
        mockData.put("StandardUser", standardUser);
        
        mockDatabase.put("LoginData", mockData);
        
        Map<String, Map<String, String>> checkoutData = new HashMap<>();
        Map<String, String> checkout1 = new HashMap<>();
        checkout1.put("FirstName", "Laiba");
        checkout1.put("LastName", "Naz");
        checkout1.put("PostalCode", "12345");
        checkoutData.put("Checkout1", checkout1);
        
        Map<String, String> checkout2 = new HashMap<>();
        checkout2.put("FirstName", "Maleeha");
        checkout2.put("LastName", "Saleem");
        checkout2.put("PostalCode", "54321");
        checkoutData.put("Checkout2", checkout2);
        
        mockDatabase.put("CheckoutData", checkoutData);
    }
    
    public static Map<String, String> getTestData(String testCase, String tableName) {
        Map<String, String> mockData = getMockData(testCase, tableName);
        
        if (mockData != null && !mockData.isEmpty()) {
            System.out.println("Using Mock Data for: " + testCase + " in " + tableName);
            return mockData;
        }
        
        try {
            connect();
            
            if (!useMock && connection != null && !connection.isClosed()) {
                System.out.println("Trying Real Database for: " + testCase);
                return getRealDatabaseData(testCase, tableName);
            }
            
        } catch (Exception e) {
            System.out.println("Real DB failed: " + e.getMessage());
        }
        
        System.out.println("Falling back to Excel for: " + testCase);
        return getExcelData(testCase, tableName);
    }
    
    private static Map<String, String> getMockData(String testCase, String tableName) {
        try {
            Map<String, Map<String, String>> tableData = mockDatabase.get(tableName);
            if (tableData != null) {
                Map<String, String> testCaseData = tableData.get(testCase);
                if (testCaseData != null && !testCaseData.isEmpty()) {
                    return new HashMap<>(testCaseData);
                }
            }
        } catch (Exception e) {
            System.out.println("Mock data error: " + e.getMessage());
        }
        return null;
    }
    
    public static void connect() {
        if (useMock) {
            System.out.println("Using Mock Database");
            return;
        }
        
        try {
            System.out.println("Connecting to XAMPP MySQL: " + DB_URL);
            
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            connection = DriverManager.getConnection(DB_URL + "?useSSL=false&serverTimezone=UTC", DB_USER, DB_PASSWORD);
            
            if (connection != null && !connection.isClosed()) {
                System.out.println("Connected to saucedemo_test database!");
                printDatabaseInfo();
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver not found.");
            System.out.println("Add to pom.xml: mysql-connector-java:8.0.33");
            useMock = true;
        } catch (SQLException e) {
            System.out.println("Database Connection Failed: " + e.getMessage());
            System.out.println("XAMPP SETUP INSTRUCTIONS:");
            System.out.println("1. Start XAMPP -> Start MySQL");
            System.out.println("2. Open phpMyAdmin: http://localhost/phpmyadmin");
            System.out.println("3. Import SQL: src/test/resources/database/setup.sql");
            System.out.println("4. OR Run these commands in MySQL:");
            System.out.println("   CREATE DATABASE saucedemo_test;");
            System.out.println("   USE saucedemo_test;");
            System.out.println("   [Copy paste the SQL script]");
            System.out.println("Using Mock Database for now...");
            useMock = true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            useMock = true;
        }
    }
    
    private static Map<String, String> getRealDatabaseData(String testCase, String tableName) throws SQLException {
        Map<String, String> data = new HashMap<>();
        
        String dbTable = mapToDatabaseTable(tableName);
        String query = "SELECT * FROM " + dbTable + " WHERE test_case = ?";
        
        System.out.println("Executing DB Query: " + query + " [" + testCase + "]");
        
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, testCase);
        ResultSet rs = pstmt.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        
        if (rs.next()) {
            System.out.println("Data found in saucedemo_test database!");
            
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                String value = rs.getString(i);
                if (value != null) {
                    data.put(columnName, value);
                }
            }
            
        } else {
            System.out.println("No data in DB for: " + testCase);
        }
        
        rs.close();
        pstmt.close();
        return data;
    }
    
    private static String mapToDatabaseTable(String sheetName) {
        Map<String, String> tableMap = new HashMap<>();
        tableMap.put("LoginData", "login_test_data");
        tableMap.put("CartData", "cart_test_data");
        tableMap.put("CheckoutData", "checkout_test_data");
        tableMap.put("SortData", "sort_preferences");
        tableMap.put("users", "login_test_data");
        tableMap.put("sort_preferences", "sort_preferences");
        
        return tableMap.getOrDefault(sheetName, sheetName);
    }
    
    private static Map<String, String> getExcelData(String testCase, String sheetName) {
        try (ExcelReader excelReader = new ExcelReader(
                "src/test/resources/testdata/test-data.xlsx", sheetName)) {
            
            Map<String, String> data = excelReader.getTestData(testCase);
            if (data != null && !data.isEmpty()) {
                System.out.println("Using Excel data for: " + testCase);
                return data;
            }
            
        } catch (Exception e) {
            System.out.println("Excel error: " + e.getMessage());
        }
        
        System.out.println("Using hardcoded fallback for: " + testCase);
        return new HashMap<>();
    }
    
    private static void printDatabaseInfo() throws SQLException {
        String query = "SELECT table_name, table_rows FROM information_schema.tables " +
                      "WHERE table_schema = 'saucedemo_test'";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        System.out.println("SAUCEDEMO_TEST DATABASE TABLES:");
        System.out.println("=================================");
        while (rs.next()) {
            System.out.printf("%-20s | %5s records%n", 
                rs.getString("table_name"), 
                rs.getString("table_rows"));
        }
        System.out.println("=================================");
        
        rs.close();
        stmt.close();
    }
    
    public static void testConnection() {
        System.out.println("=== TESTING SAUCEDEMO_TEST DATABASE ===");
        connect();
        
        if (!useMock) {
            try {
                String query = "SELECT test_case, username FROM login_test_data LIMIT 3";
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                
                System.out.println("Sample Login Data:");
                while (rs.next()) {
                    System.out.println("  " + rs.getString("test_case") + " - " + rs.getString("username"));
                }
                
                rs.close();
                stmt.close();
                
            } catch (Exception e) {
                System.out.println("Test query failed: " + e.getMessage());
            }
        }
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (Exception e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}