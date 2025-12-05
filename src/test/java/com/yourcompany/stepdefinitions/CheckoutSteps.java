package com.yourcompany.stepdefinitions;

import com.yourcompany.hooks.Hooks;
import com.yourcompany.pages.CartPage;
import com.yourcompany.pages.CheckoutPage;
import com.yourcompany.utils.DataSourceManager;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// ✅ ALLURE IMPORT ADD
import io.qameta.allure.Step;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class CheckoutSteps {
    private WebDriver driver;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;
    private WebDriverWait wait;

    public CheckoutSteps() {
        this.driver = Hooks.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.cartPage = new CartPage(driver);
        this.checkoutPage = new CheckoutPage(driver);
    }

    @Step("Proceeding to checkout") // ✅ ALLURE STEP
    @When("I proceed to checkout")
    public void i_proceed_to_checkout() throws Exception {
        driver.get("https://www.saucedemo.com/cart.html");
        Thread.sleep(2000);

        try {
            String cartCount = driver.findElement(By.className("shopping_cart_badge")).getText();
            if (cartCount.equals("0")) {
                driver.get("https://www.saucedemo.com/inventory.html");
                Thread.sleep(1000);
                driver.findElement(By.cssSelector(".btn_inventory")).click();
                Thread.sleep(1000);
                driver.get("https://www.saucedemo.com/cart.html");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            driver.get("https://www.saucedemo.com/inventory.html");
            Thread.sleep(1000);
            driver.findElement(By.cssSelector(".btn_inventory")).click();
            Thread.sleep(1000);
            driver.get("https://www.saucedemo.com/cart.html");
            Thread.sleep(2000);
        }

        cartPage.clickCheckout();
        Thread.sleep(2000);
    }

    @Step("Filling checkout information") // ✅ ALLURE STEP
    @And("I fill checkout information")
    public void i_fill_checkout_information() throws Exception {
        checkoutPage.fillCheckoutForm("Test", "User", "12345");
        Thread.sleep(1000);
        checkoutPage.clickContinue();
        Thread.sleep(3000);
    }

    @Step("Completing purchase") // ✅ ALLURE STEP
    @And("I complete purchase")
    public void i_complete_purchase() throws Exception {
        checkoutPage.clickFinish();
    }

    @Step("Verifying order confirmation message") // ✅ ALLURE STEP
    @Then("I should see order confirmation")
    public void i_should_see_order_confirmation() {
        String message = checkoutPage.getConfirmationMessage();
        Assert.assertTrue(message.toLowerCase().contains("thank you"));
    }

    @Step("Clicking checkout button") // ✅ ALLURE STEP
    private void clickCheckoutButton() throws Exception {
        List<By> selectors = Arrays.asList(
            By.id("checkout"),
            By.cssSelector("button[data-test='checkout']"),
            By.xpath("//button[contains(text(), 'Checkout')]"),
            By.xpath("//a[contains(text(), 'Checkout')]")
        );

        boolean clicked = false;
        for (By selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(selector);
                if (!elements.isEmpty()) {
                    elements.get(0).click();
                    clicked = true;
                    Thread.sleep(2000);
                    break;
                }
            } catch (Exception ignored) {}
        }

        if (!clicked) {
            throw new RuntimeException("Checkout button not found with any selector");
        }
    }

    @Step("Cancelling checkout") // ✅ ALLURE STEP
    @And("I cancel checkout")
    public void i_cancel_checkout() throws Exception {
        if (!checkoutPage.isOnCheckoutStepOne()) {
            cartPage.clickCheckout();
            Thread.sleep(2000);
        }
        checkoutPage.clickCancel();
        Thread.sleep(2000);
    }

    @Step("Verifying return to cart page") // ✅ ALLURE STEP
    @Then("I should return to cart page")
    public void i_should_return_to_cart_page() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        boolean onCartPage = wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assert.assertTrue(onCartPage);

        try {
            boolean hasCartTitle = driver.findElement(By.className("title")).getText()
                    .toLowerCase().contains("cart");
            Assert.assertTrue(hasCartTitle);
            try {
                driver.findElement(By.className("cart_list")).isDisplayed();
            } catch (Exception e) {
                driver.findElement(By.className("cart_contents_container")).isDisplayed();
            }
        } catch (Exception e) {
            throw new AssertionError("Cart page verification failed: " + e.getMessage());
        }
    }

    @Step("Verifying order confirmation") // ✅ ALLURE STEP
    @Then("order should be confirmed")
    public void order_should_be_confirmed() {
        String currentUrl = driver.getCurrentUrl();
        if (!currentUrl.contains("checkout-complete")) {
            throw new AssertionError("Order not confirmed. Expected checkout-complete page.");
        }
    }

    @Step("Checking out with Excel data for test case: {testCase}") // ✅ ALLURE STEP
    @When("I checkout with Excel data for {string}")
    public void i_checkout_with_excel_data_for(String testCase) throws Exception {
        navigateToCartPage();
        ensureCartHasItems();
        Thread.sleep(2000);

        if (!driver.getCurrentUrl().contains("checkout")) {
            driver.findElement(By.id("checkout")).click();
            Thread.sleep(3000);
        }

        fillCheckoutFormWithExcel(testCase);
        completeCheckout();
    }

    @Step("Navigating to cart page") // ✅ ALLURE STEP
    private void navigateToCartPage() throws Exception {
        driver.get("https://www.saucedemo.com/cart.html");
        Thread.sleep(3000);

        if (!driver.getCurrentUrl().contains("cart")) {
            throw new RuntimeException("Failed to navigate to cart page");
        }
    }

    @Step("Ensuring cart has items") // ✅ ALLURE STEP
    private void ensureCartHasItems() throws Exception {
        try {
            WebElement cartBadge = driver.findElement(By.className("shopping_cart_badge"));
            String itemCount = cartBadge.getText();
            if (itemCount.equals("0")) {
                addItemToCart();
            }
        } catch (NoSuchElementException e) {
            addItemToCart();
        }
    }

    @Step("Adding item to cart") // ✅ ALLURE STEP
    private void addItemToCart() throws Exception {
        driver.get("https://www.saucedemo.com/inventory.html");
        Thread.sleep(2000);
        driver.findElements(By.cssSelector(".btn_inventory")).get(0).click();
        Thread.sleep(1500);
        driver.get("https://www.saucedemo.com/cart.html");
        Thread.sleep(2000);
    }

    @Step("Completing checkout process") // ✅ ALLURE STEP
    private void completeCheckout() throws Exception {
        Thread.sleep(2000);
        if (driver.getCurrentUrl().contains("checkout-step-two")) {
            driver.findElement(By.id("finish")).click();
            Thread.sleep(2000);
        }
    }

    @Step("Filling checkout form with Excel data for test case: {testCase}") // ✅ ALLURE STEP
    private void fillCheckoutFormWithExcel(String testCase) throws Exception {
        String currentUrl = driver.getCurrentUrl();

        if (!currentUrl.contains("checkout-step-one")) {
            driver.get("https://www.saucedemo.com/cart.html");
            Thread.sleep(1500);
            try {
                driver.findElement(By.id("checkout")).click();
                Thread.sleep(2500);
            } catch (Exception e) {
                driver.get("https://www.saucedemo.com/checkout-step-one.html");
                Thread.sleep(2500);
            }
        }

        String firstName = DataSourceManager.getData(testCase, "FirstName", "CheckoutData");
        String lastName = DataSourceManager.getData(testCase, "LastName", "CheckoutData");
        String postalCode = DataSourceManager.getData(testCase, "PostalCode", "CheckoutData");

        if (firstName == null || firstName.isEmpty()) {
            firstName = "Test";
            lastName = "User";
            postalCode = "12345";
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement firstNameField = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("first-name"))
            );
            WebElement lastNameField = driver.findElement(By.id("last-name"));
            WebElement postalCodeField = driver.findElement(By.id("postal-code"));

            firstNameField.clear();
            firstNameField.sendKeys(firstName);
            lastNameField.clear();
            lastNameField.sendKeys(lastName);
            postalCodeField.clear();
            postalCodeField.sendKeys(postalCode);

            checkoutPage.clickContinue();

        } catch (Exception e) {
            checkoutPage.fillCheckoutForm(firstName, lastName, postalCode);
            checkoutPage.clickContinue();
        }
    }
}