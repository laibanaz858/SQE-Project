package com.yourcompany.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;
    private static ConfigReader instance;
   
    public ConfigReader() {
        properties = new Properties();
        loadProperties();
    }
   
    public static ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }
    
    private void loadProperties() {
        String env = System.getProperty("env", "STAGING").toUpperCase();
        String propertyFilePath = "src/test/resources/config/" + 
                                 env.toLowerCase() + ".properties";
        
        if (!new java.io.File(propertyFilePath).exists()) {
            propertyFilePath = "src/test/resources/config/environment.properties";
        }
        
        if (!new java.io.File(propertyFilePath).exists()) {
            propertyFilePath = "src/test/resources/config.properties";
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(propertyFilePath))) {
            properties.load(reader);
            System.out.println("Loaded configuration from: " + propertyFilePath);
            
            System.out.println("Configuration loaded:");
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                if (key.toLowerCase().contains("password")) {
                    System.out.println("  " + key + " = [HIDDEN]");
                } else {
                    System.out.println("  " + key + " = " + value);
                }
            }
            
        } catch (IOException e) {
            System.out.println("Configuration file not found: " + propertyFilePath);
            System.out.println("Using default values");
            
            setDefaultProperties();
        }
    }
    
    private void setDefaultProperties() {
        properties.setProperty("excel.file.path", "src/test/resources/testdata/test-data.xlsx");
        properties.setProperty("default.data.source", "excel");
        
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/test_automation");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("db.pool.size", "5");
        
        properties.setProperty("redis.host", "localhost");
        properties.setProperty("redis.port", "6379");
        properties.setProperty("redis.timeout", "5000");
        properties.setProperty("redis.pool.size", "10");
        
        properties.setProperty("base.url", "https://www.saucedemo.com");
        properties.setProperty("browser", "chrome");
        properties.setProperty("headless", "false");
        properties.setProperty("implicit.wait", "10");
        properties.setProperty("explicit.wait", "20");
        properties.setProperty("page.load.timeout", "30");
        
        properties.setProperty("allure.enabled", "true");
        properties.setProperty("cucumber.publish.enabled", "false");
        properties.setProperty("screenshot.on.failure", "true");
        properties.setProperty("parallel.threads", "4");
        
        System.out.println("Default configuration set");
    }
    
    public static String getProperty(String key, String defaultValue) {
        if (properties == null) {
            getInstance();
        }
        String value = properties.getProperty(key);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }
    
    public static String getProperty(String key) {
        if (properties == null) {
            getInstance();
        }
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Configuration property '" + key + "' not found");
        }
        return value.trim();
    }
    
    public static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            System.out.println("Invalid integer for property '" + key + "', using default: " + defaultValue);
            return defaultValue;
        }
    }
    
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
    
    public static long getLongProperty(String key, long defaultValue) {
        try {
            return Long.parseLong(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            System.out.println("Invalid long for property '" + key + "', using default: " + defaultValue);
            return defaultValue;
        }
    }
    
    public static String getBaseUrl() {
        return getProperty("base.url", "https://www.saucedemo.com");
    }

    public static String getBrowser() {
        return getProperty("browser", "chrome").toLowerCase();
    }
    
    public static long getImplicitWait() {
        return getLongProperty("implicit.wait", 10);
    }
    
    public static long getExplicitWait() {
        return getLongProperty("explicit.wait", 15);
    }
    
    public static boolean isHeadless() {
        return getBooleanProperty("headless", false);
    }
    
    public static boolean isCucumberPublishEnabled() {
        return getBooleanProperty("cucumber.publish.enabled", false);
    }
    
    public static boolean isAllureEnabled() {
        return getBooleanProperty("allure.enabled", true);
    }
    
    public static int getParallelThreads() {
        return getIntProperty("parallel.threads", 4);
    }
    
    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:mysql://localhost:3306/test_automation");
    }
    
    public static String getDbUsername() {
        return getProperty("db.username", "root");
    }
    
    public static String getDbPassword() {
        return getProperty("db.password", "");
    }
    
    public static String getDbDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }
    
    public static int getDbPoolSize() {
        return getIntProperty("db.pool.size", 5);
    }
    
    public static String getRedisHost() {
        return getProperty("redis.host", "localhost");
    }
    
    public static int getRedisPort() {
        return getIntProperty("redis.port", 6379);
    }
    
    public static int getRedisTimeout() {
        return getIntProperty("redis.timeout", 5000);
    }
    
    public static int getRedisPoolSize() {
        return getIntProperty("redis.pool.size", 10);
    }
    
    public static String getExcelFilePath() {
        return getProperty("excel.file.path", "src/test/resources/testdata/test-data.xlsx");
    }
    
    public static String getDefaultDataSource() {
        return getProperty("default.data.source", "excel");
    }
    
    public static void reloadProperties() {
        instance = null;
        getInstance();
    }
    
    public static void printAllProperties() {
        if (properties == null) {
            getInstance();
        }
        System.out.println("=== CURRENT CONFIGURATION ===");
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (key.toLowerCase().contains("password") || key.toLowerCase().contains("secret")) {
                System.out.println(key + " = [HIDDEN]");
            } else {
                System.out.println(key + " = " + value);
            }
        }
        System.out.println("=============================");
    }
    
    public static void main(String[] args) {
        System.out.println("=== Testing ConfigReader ===");
        
        System.out.println("Base URL: " + getBaseUrl());
        System.out.println("Browser: " + getBrowser());
        System.out.println("Excel Path: " + getExcelFilePath());
        System.out.println("Redis Host: " + getRedisHost() + ":" + getRedisPort());
        System.out.println("DB URL: " + getDbUrl());
        
        printAllProperties();
    }
}