package com.yourcompany.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Scenario;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import com.yourcompany.utils.RedisUtil;

// ✅ ALLURE IMPORTS ADD KAREIN
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;

public class Hooks {
    private static WebDriver driver;
    private static boolean redisConnected = false;
    
    @BeforeAll
    public static void globalSetup() {
        System.out.println("=== GLOBAL TEST SETUP ===");
        System.out.println("Initializing WebDriverManager...");
        WebDriverManager.chromedriver().setup();
    }

    @Before("@redis")
    public void setupRedis() {
        System.out.println("=== REDIS SPECIFIC SETUP ===");
        if (!redisConnected) {
            RedisUtil.connect();
            redisConnected = true;
        }
        System.out.println("✅ Redis ready for caching operations");
    }
    
    @Before("@performance")
    public void setupPerformanceTest() {
        System.out.println("⚡ PERFORMANCE TEST MODE ACTIVATED");
        System.out.println("Recording timestamps for performance measurement...");
    }
    
    @Before  // ✅ ALLURE SCENARIO NAME SET KAREIN
    public void setAllureScenarioName(Scenario scenario) {
        // Allure mein scenario name set karein
        Allure.getLifecycle().updateTestCase(testResult -> 
            testResult.setName(scenario.getName()));
        
        // Scenario description bhi add kar sakte hain
        if (scenario.getSourceTagNames() != null && !scenario.getSourceTagNames().isEmpty()) {
            String tags = String.join(", ", scenario.getSourceTagNames());
            Allure.getLifecycle().updateTestCase(testResult -> 
                testResult.setDescription("Tags: " + tags));
        }
    }
    
    @After("@redis")
    public void cleanupRedis(Scenario scenario) {
        System.out.println("=== REDIS CLEANUP ===");
        
        if (scenario.isFailed()) {
            System.out.println("❌ Test failed - preserving Redis data for debugging");
            RedisUtil.saveFailedTestData(scenario.getName(), driver.getCurrentUrl());
        } else {
            RedisUtil.clearTestData(scenario.getName());
            System.out.println("🧹 Redis test data cleared");
        }
        
        RedisUtil.logScenarioEnd(scenario.getName(), scenario.getStatus().toString());
    }
    
    @After("@performance")
    public void logPerformanceMetrics(Scenario scenario) {
        System.out.println("📊 PERFORMANCE METRICS:");
        System.out.println("Scenario: " + scenario.getName());
        System.out.println("Status: " + scenario.getStatus());
        System.out.println("Page load time: [to be implemented]");
        System.out.println("API response time: [to be implemented]");
    }
    
    @After
    public void teardown(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                captureScreenshot(scenario);
                captureAllureScreenshot(scenario); // ✅ ALLURE SCREENSHOT
                logFailureDetails(scenario);
            }
            
            if (redisConnected && scenario.getSourceTagNames().contains("@redis")) {
                RedisUtil.logScenarioCompletion(scenario.getName(), 
                    scenario.getStatus().toString(),
                    driver.getCurrentUrl());
            }
            
        } catch (Exception e) {
            System.out.println("⚠️ Error during teardown: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
                System.out.println("🔴 Browser closed");
            }
            
            System.out.println("=== Completed: " + scenario.getName() + " ===\n");
        }
    }
    
    // ✅ NEW METHOD: ALLURE SCREENSHOT
    private void captureAllureScreenshot(Scenario scenario) {
        try {
            if (driver != null) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(
                    "Screenshot on Failure - " + scenario.getName(),
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
                );
                System.out.println("📸 Allure screenshot attached");
            }
        } catch (Exception e) {
            System.out.println("Could not attach Allure screenshot: " + e.getMessage());
        }
    }
    
    @AfterAll
    public static void globalTeardown() {
        System.out.println("=== GLOBAL TEST TEARDOWN ===");
        if (redisConnected) {
            RedisUtil.close();
            System.out.println("🔌 Redis connection closed");
        }
        
        generateTestSummary();
        
        System.out.println("=== ALL TESTS COMPLETED ===");
    }
    
    private void captureScreenshot(Scenario scenario) {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Failed_" + scenario.getName());
            System.out.println("📸 Screenshot captured for failed scenario");
        } catch (Exception e) {
            System.out.println("❌ Failed to capture screenshot: " + e.getMessage());
        }
    }
    
    private void logFailureDetails(Scenario scenario) {
        System.out.println("\n❌ FAILURE DETAILS:");
        System.out.println("Scenario: " + scenario.getName());
        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("Title: " + driver.getTitle());
        
        // ✅ ALLURE MEIN FAILURE DETAILS ADD KAREIN
        Allure.addAttachment("Failure Details", 
            "Scenario: " + scenario.getName() + "\n" +
            "URL: " + driver.getCurrentUrl() + "\n" +
            "Title: " + driver.getTitle() + "\n" +
            "Status: " + scenario.getStatus()
        );
        
        if (redisConnected) {
            RedisUtil.logFailure(
                scenario.getName(),
                driver.getCurrentUrl(),
                driver.getTitle()
            );
        }
    }
    
    private static void generateTestSummary() {
        System.out.println("\n📋 TEST EXECUTION SUMMARY:");
        System.out.println("==========================");
        
        if (redisConnected) {
            String redisStats = RedisUtil.getTestStats();
            System.out.println(redisStats);
        }
        
        System.out.println("==========================");
        System.out.println("End Time: " + new java.util.Date());
    }
    
    public static WebDriver getDriver() {
        return driver;
    }
    
    public static boolean isRedisConnected() {
        return redisConnected;
    }
    
    @Before
    public void setup(Scenario scenario) {
        System.out.println("\n=== Starting: " + scenario.getName() + " ===");
        
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        
        driver = new ChromeDriver(options);
        
        try {
            driver.get("https://www.saucedemo.com");
            System.out.println("Website loaded: " + driver.getCurrentUrl());
            
            // ✅ ALLURE MEIN STEP LOGGING
            Allure.step("Navigated to: " + driver.getCurrentUrl());
            
        } catch (Exception e) {
            System.out.println("Internet issue: " + e.getMessage());
            System.out.println("Running in offline mode...");
            driver.get("data:text/html,<h1>Test Mode - Internet Not Available</h1>");
            
            Allure.step("Internet not available, running in offline mode");
        }
    }
}