package com.yourcompany.utils;

import java.util.HashMap;
import java.util.Map;

public class DataSourceManager {

    public enum Source {
        EXCEL, DATABASE, REDIS, AUTO
    }

    private static Source currentSource = Source.AUTO;
    private static String currentTestType = "";

    public static void configureSource(String testType) {
        currentTestType = testType.toLowerCase();

        if (currentSource == Source.AUTO) {
            switch (currentTestType) {
                case "login":
                case "cart":
                case "search":
                    currentSource = Source.EXCEL;
                    break;
                case "checkout":
                case "payment":
                case "order":
                    currentSource = Source.DATABASE;
                    break;
                case "performance":
                case "load":
                case "cache":
                    currentSource = Source.REDIS;
                    break;
                default:
                    currentSource = Source.EXCEL;
            }
        }

        System.out.println("Data Source: " + currentSource + " for test: " + testType);
    }

    public static void setSource(Source source) {
        currentSource = source;
        System.out.println("Manual source set to: " + source);
    }

    public static Source getCurrentSource() {
        return currentSource;
    }

    private static String convertToTableName(String sheetName) {
        if (sheetName == null) {
            return "login_test_data";
        }

        switch (sheetName.toLowerCase()) {
            case "users":
            case "logindata":
            case "userdata":
                return "login_test_data";
            case "cartdata":
                return "cart_test_data";
            case "checkoutdata":
                return "checkout_test_data";
            case "sortdata":
                return "sort_preferences";
            case "productdata":
                return "product_test_data";
            default:
                if (sheetName.toLowerCase().contains("test")) {
                    return sheetName;
                }
                return "login_test_data";
        }
    }

    public static String getData(String testCase, String columnName, String sheetName) {
        try {
            System.out.println("Data request - TestCase: " + testCase + 
                             ", Column: " + columnName + ", Sheet: " + sheetName);

            switch (currentSource) {
                case EXCEL:
                    System.out.println("Using Excel data from sheet: " + sheetName);
                    String excelData = getExcelData(testCase, columnName, sheetName);

                    if (excelData == null || excelData.isEmpty()) {
                        System.out.println("Excel data not found, using hardcoded fallback");
                        return getHardcodedData(testCase, columnName, sheetName);
                    }
                    return excelData;

                case DATABASE:
                    String tableName = convertToTableName(sheetName);
                    System.out.println("Using Database data from table: " + tableName);
                    
                    Map<String, String> dbData = DatabaseUtil.getTestData(testCase, tableName);

                    if (dbData != null && !dbData.isEmpty()) {
                        System.out.println("Database returned data with keys: " + dbData.keySet());
                        
                        String value = dbData.get(columnName);
                        if (value == null) {
                            value = dbData.get(columnName.toLowerCase());
                        }
                        if (value == null) {
                            for (Map.Entry<String, String> entry : dbData.entrySet()) {
                                if (entry.getKey().toLowerCase().contains(columnName.toLowerCase())) {
                                    value = entry.getValue();
                                    break;
                                }
                            }
                        }

                        if (value != null && !value.isEmpty()) {
                            Map<String, String> cacheData = new HashMap<>();
                            cacheData.put(columnName, value);
                            RedisUtil.cacheData(testCase + ":" + sheetName, cacheData, 300);
                            return value;
                        } else {
                            System.out.println("Column '" + columnName + "' not found in database data");
                        }
                    } else {
                        System.out.println("Database returned null or empty data");
                    }
                    
                    System.out.println("Falling back to Excel data");
                    return getDataFromFallback(testCase, columnName, sheetName);

                case REDIS:
                    System.out.println("Using Redis cache for key: " + testCase + ":" + sheetName);
                    Map<String, String> redisData = RedisUtil.getCachedData(testCase + ":" + sheetName);

                    if (redisData != null && redisData.containsKey(columnName)) {
                        String value = redisData.get(columnName);
                        if (value != null && !value.isEmpty()) {
                            return value;
                        }
                    }
                    System.out.println("Redis empty, falling back");
                    return getDataFromFallback(testCase, columnName, sheetName);

                default:
                    System.out.println("Using AUTO mode, defaulting to Excel");
                    return getDataFromFallback(testCase, columnName, sheetName);
            }
        } catch (Exception e) {
            System.out.println("Error in " + currentSource + " for " + testCase + 
                             ", column: " + columnName + ": " + e.getMessage());
            System.out.println("Using hardcoded fallback");
            return getHardcodedData(testCase, columnName, sheetName);
        }
    }

    private static String getDataFromFallback(String testCase, String columnName, String sheetName) {
        try {
            System.out.println("Attempting Excel fallback for: " + testCase);
            String excelData = getExcelData(testCase, columnName, sheetName);
            if (excelData != null && !excelData.isEmpty()) {
                return excelData;
            }
        } catch (Exception e) {
            System.out.println("Excel fallback failed: " + e.getMessage());
        }

        System.out.println("Using hardcoded data as final fallback");
        return getHardcodedData(testCase, columnName, sheetName);
    }

    private static String getExcelData(String testCase, String columnName, String sheetName) {
        try {
            ExcelReader excelReader = new ExcelReader("src/test/resources/testdata/test-data.xlsx", sheetName);
            String data = excelReader.getData(testCase, columnName, sheetName);
            excelReader.close();
            
            if (data == null || data.isEmpty()) {
                System.out.println("Excel data not found for: " + testCase + " -> " + columnName);
                return null;
            }
            return data;

        } catch (Exception e) {
            System.out.println("Excel error for sheet '" + sheetName + "': " + e.getMessage());
            return null;
        }
    }

    private static String getHardcodedData(String testCase, String columnName, String sheetName) {
        System.out.println("Using hardcoded data for: " + testCase + " -> " + columnName + " in " + sheetName);

        if ("LoginData".equalsIgnoreCase(sheetName) || "users".equalsIgnoreCase(sheetName)) {
            if ("ValidUser".equalsIgnoreCase(testCase)) {
                if ("Username".equalsIgnoreCase(columnName) || "username".equalsIgnoreCase(columnName))
                    return "standard_user";
                if ("Password".equalsIgnoreCase(columnName) || "password".equalsIgnoreCase(columnName))
                    return "secret_sauce";
            }
            if ("AdminUser".equalsIgnoreCase(testCase)) {
                if ("Username".equalsIgnoreCase(columnName) || "username".equalsIgnoreCase(columnName))
                    return "admin_user";
                if ("Password".equalsIgnoreCase(columnName) || "password".equalsIgnoreCase(columnName))
                    return "admin_pass";
            }
            if ("InvalidUser".equalsIgnoreCase(testCase)) {
                if ("Username".equalsIgnoreCase(columnName) || "username".equalsIgnoreCase(columnName))
                    return "invalid_user";
                if ("Password".equalsIgnoreCase(columnName) || "password".equalsIgnoreCase(columnName))
                    return "wrong_password";
            }
            if ("LockedUser".equalsIgnoreCase(testCase)) {
                if ("Username".equalsIgnoreCase(columnName) || "username".equalsIgnoreCase(columnName))
                    return "locked_out_user";
                if ("Password".equalsIgnoreCase(columnName) || "password".equalsIgnoreCase(columnName))
                    return "secret_sauce";
            }
        }

        if ("CartData".equalsIgnoreCase(sheetName)) {
            if ("CartItems".equalsIgnoreCase(testCase) && "ItemCount".equalsIgnoreCase(columnName))
                return "3";
            if ("CartCache".equalsIgnoreCase(testCase) && "ItemCount".equalsIgnoreCase(columnName))
                return "2";
        }

        if ("CheckoutData".equalsIgnoreCase(sheetName)) {
            if ("CheckoutTest".equalsIgnoreCase(testCase)) {
                if ("FirstName".equalsIgnoreCase(columnName))
                    return "John";
                if ("LastName".equalsIgnoreCase(columnName))
                    return "Doe";
                if ("PostalCode".equalsIgnoreCase(columnName))
                    return "12345";
            }
            if ("AdminCheckout".equalsIgnoreCase(testCase)) {
                if ("FirstName".equalsIgnoreCase(columnName))
                    return "Jane";
                if ("LastName".equalsIgnoreCase(columnName))
                    return "Smith";
                if ("PostalCode".equalsIgnoreCase(columnName))
                    return "67890";
            }
        }

        if ("SortData".equalsIgnoreCase(sheetName)) {
            if ("PriceFilter".equalsIgnoreCase(testCase) && "Filter".equalsIgnoreCase(columnName))
                return "Price (high to low)";
            if ("SortPreference".equalsIgnoreCase(testCase) && "Filter".equalsIgnoreCase(columnName))
                return "Name (A to Z)";
        }

        if ("username".equalsIgnoreCase(columnName)) {
            return "standard_user";
        } else if ("password".equalsIgnoreCase(columnName)) {
            return "secret_sauce";
        } else if ("expected".equalsIgnoreCase(columnName)) {
            return "success";
        }

        return "";
    }

    public static Map<String, String> getData(String testCase, String dataSet) {
        try {
            ExcelReader excelReader = new ExcelReader("src/test/resources/testdata/test-data.xlsx", dataSet);
            Map<String, String> data = excelReader.getTestData(testCase);
            excelReader.close();
            return data;
        } catch (Exception e) {
            System.out.println("Error in getData(2 params): " + e.getMessage());

            Map<String, String> hardcodedData = new HashMap<>();

            if (("LoginData".equalsIgnoreCase(dataSet) || "users".equalsIgnoreCase(dataSet)) 
                && "ValidUser".equalsIgnoreCase(testCase)) {
                hardcodedData.put("Username", "standard_user");
                hardcodedData.put("Password", "secret_sauce");
                hardcodedData.put("Expected", "success");
            }

            return hardcodedData;
        }
    }
}