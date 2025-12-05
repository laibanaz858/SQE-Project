package com.yourcompany.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

public class CheckoutPage extends BasePage {

    @FindBy(id = "first-name")
    private WebElement firstNameField;

    @FindBy(id = "last-name")
    private WebElement lastNameField;

    @FindBy(id = "postal-code")
    private WebElement postalCodeField;

    @FindBy(id = "continue")
    private WebElement continueButton;

    @FindBy(id = "finish")
    private WebElement finishButton;

    @FindBy(className = "complete-header")
    private WebElement confirmationMessage;

    @FindBy(id = "cancel")
    private WebElement cancelButton;

    @FindBy(css = "[data-test='error']")
    private WebElement errorMessage;

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public void fillCheckoutForm(String firstName, String lastName, String postalCode) {
        try {
            waitForPageToLoad();
            sleep(1000);

            if (!isOnCheckoutStepOne()) {
                throw new RuntimeException("Not on checkout step one");
            }

            fillFieldProperly(firstNameField, firstName);
            fillFieldProperly(lastNameField, lastName);
            fillFieldProperly(postalCodeField, postalCode);

            sleep(1000);
        } catch (Exception e) {
            throw e;
        }
    }

    private void fillFieldProperly(WebElement field, String value) {
        try {
            waitForElement(field);
            field.clear();
            sleep(200);
            field.click();
            sleep(200);

            for (char c : value.toCharArray()) {
                field.sendKeys(String.valueOf(c));
                sleep(50);
            }

            js.executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));" +
                "arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));",
                field
            );

            sleep(300);
        } catch (Exception e) {
            throw e;
        }
    }

    public void clickContinue() {
        try {
            takeScreenshot("before_continue_click");
            waitForClickable(continueButton);
            continueButton.click();
            sleep(2000);
            takeScreenshot("after_continue_click");

            if (!isOnCheckoutStepTwo()) {
                checkForErrors();

                if (isOnCheckoutStepOne()) {
                    js.executeScript("arguments[0].click();", continueButton);
                    sleep(2000);

                    if (!isOnCheckoutStepTwo()) {
                        throw new RuntimeException("Failed to navigate to checkout step two");
                    }
                }
            }

        } catch (Exception e) {
            takeScreenshot("continue_error");
            throw e;
        }
    }

    public boolean isOnCheckoutStepOne() {
        try {
            boolean urlCheck = driver.getCurrentUrl().contains("checkout-step-one") ||
                    driver.getCurrentUrl().contains("checkout");
            boolean firstNameDisplayed = firstNameField.isDisplayed();
            boolean continueDisplayed = continueButton.isDisplayed();

            return urlCheck || (firstNameDisplayed && continueDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOnCheckoutStepTwo() {
        try {
            boolean urlCheck = driver.getCurrentUrl().contains("checkout-step-two");
            boolean finishDisplayed = finishButton.isDisplayed();
            return urlCheck || finishDisplayed;
        } catch (Exception e) {
            return false;
        }
    }

    private void checkForErrors() {
        try {
            sleep(1000);

            if (errorMessage.isDisplayed()) {
                String error = errorMessage.getText();
                takeScreenshot("validation_error");
                throw new RuntimeException("Validation error: " + error);
            }
        } catch (NoSuchElementException ignored) {
        } catch (Exception ignored) {
        }
    }

    public void clickFinish() {
        try {
            waitForPageToLoad();
            sleep(1000);

            if (!isOnCheckoutStepTwo()) {
                if (isCheckoutComplete()) {
                    return;
                }
                throw new RuntimeException("Not on checkout step two");
            }

            takeScreenshot("before_finish");
            clickWithJS(finishButton);
            sleep(2000);

        } catch (Exception e) {
            takeScreenshot("finish_error");
            throw e;
        }
    }

    public boolean isCheckoutComplete() {
        try {
            boolean urlCheck = driver.getCurrentUrl().contains("checkout-complete");
            boolean elementCheck = confirmationMessage.isDisplayed();
            return urlCheck || elementCheck;
        } catch (Exception e) {
            return false;
        }
    }

    public void clickCancel() {
        try {
            waitForClickable(cancelButton);
            clickWithJS(cancelButton);
            sleep(2000);
        } catch (Exception e) {
            throw e;
        }
    }

    public String getConfirmationMessage() {
        try {
            try {
                WebElement confirmation = driver.findElement(By.className("complete-header"));
                return confirmation.getText();
            } catch (Exception e1) {
                try {
                    WebElement confirmation = driver.findElement(By.cssSelector("h2.complete-header"));
                    return confirmation.getText();
                } catch (Exception e2) {
                    try {
                        WebElement confirmation = driver.findElement(By.xpath("//h2[contains(text(), 'Thank')]"));
                        return confirmation.getText();
                    } catch (Exception e3) {
                        return "Confirmation message not found";
                    }
                }
            }
        } catch (Exception e) {
            return "Error getting confirmation: " + e.getMessage();
        }
    }
}
