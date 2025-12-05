package com.yourcompany.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {
    
    @FindBy(id = "user-name")
    private WebElement usernameField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    @FindBy(id = "login-button")
    private WebElement loginButton;
    
    @FindBy(css = "h3[data-test='error']")
    private WebElement errorMessage;
    
    @FindBy(className = "login_logo")
    private WebElement loginLogo;
    
    
    public LoginPage(WebDriver driver) {
        super(driver);
    }
    

    public void login(String username, String password) {
        try {
            System.out.println("\n=== LOGIN START ===");
            System.out.println("Username: " + username);

            waitForPageToLoad();
            sleep(800);

            if (!isLoginPageDisplayed()) {
                System.out.println("Not on login page. Navigating...");
                driver.get("https://www.saucedemo.com");
                waitForPageToLoad();
                sleep(1500);
            }

            fillFieldProperly(usernameField, username);
            fillFieldProperly(passwordField, password);

            takeScreenshot("before_login_click");
            clickElement(loginButton);
            sleep(1500);
            takeScreenshot("after_login_click");

            System.out.println("Login attempt finished");
            System.out.println("=== LOGIN END ===\n");

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            takeScreenshot("login_error");
            throw e;
        }
    }


    private void fillFieldProperly(WebElement field, String value) {
        try {
            waitForElement(field);
            field.clear();
            sleep(150);

            field.click();
            sleep(150);

            for (char c : value.toCharArray()) {
                field.sendKeys(String.valueOf(c));
                sleep(40);
            }

            js.executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));" +
                "arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));",
                field
            );

            sleep(200);

        } catch (Exception e) {
            System.out.println("Error filling field: " + e.getMessage());
            throw e;
        }
    }


    public void loginWithJS(String username, String password) {
        try {
            System.out.println("Attempting JS login...");

            sendKeysWithJS(usernameField, username);
            sendKeysWithJS(passwordField, password);

            clickWithJS(loginButton);
            sleep(1500);

            takeScreenshot("login_js_click");
            System.out.println("JS login completed");

        } catch (Exception e) {
            System.out.println("JS login failed: " + e.getMessage());
            takeScreenshot("login_js_error");
            throw e;
        }
    }


    public boolean isLoginPageDisplayed() {
        try {
            boolean logo = loginLogo.isDisplayed();
            boolean button = loginButton.isDisplayed();
            boolean url = driver.getCurrentUrl().contains("saucedemo");

            System.out.println("Login page check → Logo: " + logo + 
                               ", Button: " + button + ", URL OK: " + url);

            return logo || button || url;
        } catch (Exception e) {
            return false;
        }
    }


    public boolean hasErrorMessage() {
        try {
            return errorMessage.isDisplayed() && 
                   !errorMessage.getText().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            waitForElement(errorMessage);
            return errorMessage.getText();
        } catch (Exception e) {
            return "No error message found";
        }
    }


    public void clearLoginForm() {
        try {
            usernameField.clear();
            passwordField.clear();
            System.out.println("Form cleared");
        } catch (Exception e) {
            System.out.println("Error clearing form: " + e.getMessage());
        }
    }


    public void printLoginStatus() {
        System.out.println("\n=== LOGIN STATUS ===");
        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("Login Page Visible: " + isLoginPageDisplayed());

        if (hasErrorMessage()) {
            System.out.println("Error: " + getErrorMessage());
        } else {
            System.out.println("No login error detected");
        }

        System.out.println("====================\n");
    }


    public void loginWithCredentials(String username, String password, boolean useJS) {
        if (useJS) loginWithJS(username, password);
        else login(username, password);
    }
}
