package com.yourcompany.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;


public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;
    protected Actions actions;
    
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.js = (JavascriptExecutor) driver;
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }
    
    // ==================== WAIT METHODS ====================
    
    protected void waitForElement(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }
    
    protected void waitForClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }
    
    protected void waitForPageToLoad() {
        wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
    }
    
    // ==================== ACTION METHODS ====================
    
    protected void clickElement(WebElement element) {
        waitForClickable(element);
        element.click();
    }
    
    protected void clickWithJS(WebElement element) {
        waitForElement(element);
        js.executeScript("arguments[0].click();", element);
    }
    
    protected void sendKeys(WebElement element, String text) {
        waitForElement(element);
        element.clear();
        element.sendKeys(text);
    }
    
    protected void sendKeysWithJS(WebElement element, String text) {
        waitForElement(element);
        js.executeScript("arguments[0].value = '';", element);
        js.executeScript("arguments[0].value = arguments[1];", element, text);
    }
    
    protected String getText(WebElement element) {
        waitForElement(element);
        return element.getText();
    }
    
    protected String getAttribute(WebElement element, String attribute) {
        waitForElement(element);
        return element.getAttribute(attribute);
    }
    
    protected boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    protected boolean isElementEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== SCROLL METHODS ====================
    
    protected void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", element);
        sleep(500);
    }
    
    protected void scrollToTop() {
        js.executeScript("window.scrollTo(0, 0);");
    }
    
    protected void scrollToBottom() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }
    
    // ==================== DROPDOWN METHODS ====================
    
    protected void selectByVisibleText(WebElement dropdown, String text) {
        waitForElement(dropdown);  // FIXED: Changed from 'download' to 'dropdown'
        Select select = new Select(dropdown);
        select.selectByVisibleText(text);
    }
    
    protected void selectByValue(WebElement dropdown, String value) {
        waitForElement(dropdown);
        Select select = new Select(dropdown);
        select.selectByValue(value);
    }
    
    protected String getSelectedOption(WebElement dropdown) {
        waitForElement(dropdown);
        Select select = new Select(dropdown);
        return select.getFirstSelectedOption().getText();
    }
    
    // ==================== ALERT METHODS ====================
    
    protected void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    }
    
    protected void dismissAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().dismiss();
    }
    
    protected String getAlertText() {
        wait.until(ExpectedConditions.alertIsPresent());
        return driver.switchTo().alert().getText();
    }
    
    // ==================== UTILITY METHODS ====================
    
    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    protected void takeScreenshot(String name) {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String path = "target/screenshots/" + name + "_" + System.currentTimeMillis() + ".png";
            java.nio.file.Files.write(java.nio.file.Paths.get(path), screenshot);
        } catch (Exception e) {
            System.out.println("Could not take screenshot: " + e.getMessage());
        }
    }
    
    protected void switchToFrame(WebElement frame) {
        driver.switchTo().frame(frame);
    }
    
    protected void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }
    
    protected void hoverOverElement(WebElement element) {
        waitForElement(element);
        actions.moveToElement(element).perform();
    }
    
    protected void doubleClick(WebElement element) {
        waitForElement(element);
        actions.doubleClick(element).perform();
    }
    
    protected void rightClick(WebElement element) {
        waitForElement(element);
        actions.contextClick(element).perform();
    }
    
    // ==================== VALIDATION METHODS ====================
    
    protected boolean verifyElementContainsText(WebElement element, String expectedText) {
        String actualText = getText(element);
        return actualText.contains(expectedText);
    }
    
    protected boolean verifyElementHasAttribute(WebElement element, String attribute, String expectedValue) {
        String actualValue = getAttribute(element, attribute);
        return actualValue != null && actualValue.contains(expectedValue);
    }
    
    // ==================== DEBUG METHODS ====================
    
    protected void highlightElement(WebElement element) {
        js.executeScript("arguments[0].style.border='3px solid red'", element);
        sleep(1000);
        js.executeScript("arguments[0].style.border=''", element);
    }
    
    protected void logElementInfo(WebElement element, String elementName) {
        System.out.println("\n=== Element Info: " + elementName + " ===");
        System.out.println("Text: " + element.getText());
        System.out.println("ID: " + element.getAttribute("id"));
        System.out.println("Class: " + element.getAttribute("class"));
        System.out.println("Displayed: " + element.isDisplayed());
        System.out.println("Enabled: " + element.isEnabled());
        System.out.println("Selected: " + element.isSelected());
    }
}