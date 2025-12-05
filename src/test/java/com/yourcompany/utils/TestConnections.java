package com.yourcompany.utils;

public class TestConnections {
    public static void main(String[] args) {
        System.out.println("=== TESTING ALL DATA SOURCE CONNECTIONS ===");
        System.out.println("===========================================");
        System.out.println();
        
        boolean excelPassed = false;
        boolean redisPassed = false;
        boolean dbPassed = false;
        
        System.out.println("1. TESTING EXCEL CONNECTION...");
        try {
            String excelPath = "src/test/resources/testdata/test-data.xlsx";
            System.out.println("   Excel Path: " + excelPath);
            
            ExcelReader excelReader = new ExcelReader(excelPath, "LoginData");
            String username = excelReader.getData("ValidLogin", "Username");
            String password = excelReader.getData("ValidLogin", "Password");
            
            if (username != null && !username.isEmpty()) {
                System.out.println("   EXCEL CONNECTED SUCCESSFULLY!");
                System.out.println("   Sample Data:");
                System.out.println("     Username: " + username);
                System.out.println("     Password: " + (password != null ? "[PROVIDED]" : "[EMPTY]"));
                excelPassed = true;
            } else {
                System.out.println("   EXCEL DATA EMPTY!");
            }
            
            excelReader.close();
            
        } catch (Exception e) {
            System.out.println("   EXCEL CONNECTION FAILED!");
            System.out.println("   Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        System.out.println("2. TESTING REDIS CONNECTION...");
        try {
            System.out.println("   Redis Host: localhost:6379");
            RedisUtil.testConnection();
            redisPassed = true;
            
            System.out.println("   Testing Redis data retrieval...");
            
            java.util.Map<String, String> testData = new java.util.HashMap<>();
            testData.put("Username", "test_user");
            testData.put("Password", "test_pass");
            RedisUtil.cacheData("test:ValidLogin", testData, 60);
            
            java.util.Map<String, String> redisData = RedisUtil.getCachedData("test:ValidLogin");
            if (redisData != null && !redisData.isEmpty()) {
                System.out.println("   Redis has test data");
                System.out.println("   Keys found: " + redisData.keySet());
                System.out.println("   Username: " + redisData.get("Username"));
            } else {
                System.out.println("   Redis has no test data");
            }
            
        } catch (Exception e) {
            System.out.println("   REDIS CONNECTION FAILED!");
            System.out.println("   Error: " + e.getMessage());
            System.out.println("   Tip: Make sure Redis server is running");
            System.out.println("        Run: redis-server");
        }
        
        System.out.println();
        
        System.out.println("3. TESTING DATABASE CONNECTION...");
        try {
            String dbUrl = ConfigReader.getProperty("db.url", "Not configured");
            System.out.println("   Database URL: " + dbUrl);
            
            if ("Not configured".equals(dbUrl)) {
                System.out.println("   DATABASE NOT CONFIGURED");
                System.out.println("   Add to config.properties:");
                System.out.println("     db.url=jdbc:mysql://localhost:3306/test_automation");
                System.out.println("     db.username=root");
                System.out.println("     db.password=");
            } else {
                DatabaseUtil.connect();
                System.out.println("   DATABASE CONNECTED!");
                
                System.out.println("   Testing Database data retrieval...");
                java.util.Map<String, String> dbData = DatabaseUtil.getTestData("ValidLogin", "login_test_data");
                if (dbData != null && !dbData.isEmpty()) {
                    System.out.println("   Database has test data");
                    System.out.println("   Keys found: " + dbData.keySet());
                    dbPassed = true;
                } else {
                    System.out.println("   Database has no test data (run SQL script first)");
                }
            }
            
        } catch (Exception e) {
            System.out.println("   DATABASE CONNECTION FAILED!");
            System.out.println("   Error: " + e.getMessage());
            System.out.println("   Tip: Install MySQL and create database first");
        }
        
        System.out.println();
        System.out.println("===========================================");
        System.out.println("=== CONNECTION TEST SUMMARY ===");
        System.out.println("Excel:    " + (excelPassed ? "PASS" : "FAIL"));
        System.out.println("Redis:    " + (redisPassed ? "PASS" : "FAIL")); 
        System.out.println("Database: " + (dbPassed ? "PASS" : "FAIL"));
        System.out.println("===========================================");
        
        System.out.println();
        System.out.println("=== RECOMMENDATIONS ===");
        if (!excelPassed) {
            System.out.println("1. Check Excel file exists at: src/test/resources/testdata/test-data.xlsx");
        }
        if (!redisPassed) {
            System.out.println("2. Start Redis server: redis-server");
        }
        if (!dbPassed) {
            System.out.println("3. Install MySQL/XAMPP and create database");
            System.out.println("4. Run SQL script to create tables and insert data");
        }
        
        System.out.println();
        System.out.println("Test completed!");
    }
}