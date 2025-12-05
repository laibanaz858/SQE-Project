package com.yourcompany.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.stream.Collectors;

public class CartPage extends BasePage {
    
    @FindBy(className = "title")
    private WebElement pageTitle;
    
    @FindBy(className = "shopping_cart_badge")
    private WebElement cartBadge;
    
    @FindBy(className = "shopping_cart_link")
    private WebElement cartIcon;
    
    @FindBy(className = "inventory_item_name")
    private List<WebElement> cartItemNames;
    
    @FindBy(className = "cart_item")
    private List<WebElement> cartItems;
    
    @FindBy(id = "checkout")
    private WebElement checkoutButton;
    
    @FindBy(id = "continue-shopping")
    private WebElement continueShoppingButton;
    
    public CartPage(WebDriver driver) {
        super(driver);
    }

    public void clickCartIcon() {
        clickElement(cartIcon);
        sleep(1000);
    }
    
    public String getCartItemCount() {
        try {
            return cartBadge.getText();
        } catch (Exception e) {
            return "0";
        }
    }
    
    public List<String> getCartItemNames() {
        wait.until(ExpectedConditions.visibilityOfAllElements(cartItemNames));
        return cartItemNames.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }
    
    public void removeFirstItem() {
        try {
            if (!cartItems.isEmpty()) {
                WebElement firstItem = cartItems.get(0);
                WebElement removeButton = firstItem.findElement(
                    By.xpath(".//button[contains(text(), 'Remove')]"));
                clickElement(removeButton);
                sleep(1000);
                System.out.println("Item removed from cart");
            }
        } catch (Exception e) {
            System.out.println("Error removing item: " + e.getMessage());
        }
    }
    
    public void clickCheckout() throws Exception {
        try {
            System.out.println("Clicking checkout button");
            Thread.sleep(2000);
            WebElement checkoutBtn = null;

            try {
                checkoutBtn = driver.findElement(By.id("checkout"));
            } catch (Exception e1) {
                try {
                    checkoutBtn = driver.findElement(By.cssSelector("button[data-test='checkout']"));
                } catch (Exception e2) {
                    try {
                        checkoutBtn = driver.findElement(By.xpath("//button[contains(text(), 'Checkout')]"));
                    } catch (Exception e3) {
                        throw new RuntimeException("Checkout button not found");
                    }
                }
            }
            
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkoutBtn);
            Thread.sleep(2000);
            System.out.println("Checkout button clicked");
            
        } catch (Exception e) {
            System.out.println("Error clicking checkout: " + e.getMessage());
            throw e;
        }
    }
    
    public void clickContinueShopping() {
        try {
            clickElement(continueShoppingButton);
            sleep(1000);
        } catch (Exception e) {
            System.out.println("Error clicking continue shopping: " + e.getMessage());
        }
    }
    
    public boolean isCartPageDisplayed() {
        try {
            boolean urlCheck = driver.getCurrentUrl().contains("cart");
            boolean titleCheck = pageTitle.isDisplayed() && 
                                 pageTitle.getText().toLowerCase().contains("cart");
            boolean elementsCheck = checkoutButton.isDisplayed();
            return urlCheck || titleCheck || elementsCheck;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCartEmpty() {
        try {
            cartBadge.isDisplayed();
            System.out.println("Cart badge exists - cart not empty");
            return false;
        } catch (Exception e) {
            List<WebElement> items = driver.findElements(By.className("cart_item"));
            boolean isEmpty = items.isEmpty();
            System.out.println("Cart empty check: " + isEmpty + " (items found: " + items.size() + ")");
            return isEmpty;
        }
    }

    public String getItemCount() {
        try {
            Thread.sleep(500);
            
            try {
                if (cartBadge.isDisplayed()) {
                    String count = cartBadge.getText();
                    System.out.println("Cart badge count: " + count);
                    return count;
                }
            } catch (Exception e1) {
                List<WebElement> items = driver.findElements(By.className("cart_item"));
                if (!items.isEmpty()) {
                    String count = String.valueOf(items.size());
                    System.out.println("Cart items count: " + count);
                    return count;
                }
                
                try {
                    cartIcon.isDisplayed();
                    System.out.println("Cart icon exists, no badge - count is 0");
                    return "0";
                } catch (Exception e2) {
                    return "0";
                }
            }
            return "0";
        } catch (Exception e) {
            System.out.println("Error getting cart count: " + e.getMessage());
            return "0";
        }
    }
    
    public void addItemToCartFromProductPage(String productName) {
        try {
            driver.get("https://www.saucedemo.com/inventory.html");
            sleep(1000);
            String xpath = String.format(
                "//div[contains(text(), '%s')]/ancestor::div[@class='inventory_item']//button", 
                productName
            );
            WebElement addButton = driver.findElement(By.xpath(xpath));
            clickElement(addButton);
            sleep(500);
            clickCartIcon();
        } catch (Exception e) {
            System.out.println("Error adding item: " + e.getMessage());
        }
    }
    
    public void clearCart() {
        try {
            List<WebElement> removeButtons = driver.findElements(
                By.xpath("//button[contains(text(), 'Remove')]"));
            for (WebElement button : removeButtons) {
                button.click();
                sleep(300);
            }
            System.out.println("Cart cleared");
        } catch (Exception e) {
            System.out.println("Error clearing cart: " + e.getMessage());
        }
    }
    
    public void printCartDetails() {
        System.out.println("\n=== CART DETAILS ===");
        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("Page Title: " + pageTitle.getText());
        System.out.println("Cart Badge: " + getCartItemCount());
        System.out.println("Items in cart: " + getItemCount());
        
        List<String> itemNames = getCartItemNames();
        if (!itemNames.isEmpty()) {
            System.out.println("Cart Items:");
            for (int i = 0; i < itemNames.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + itemNames.get(i));
            }
        } else {
            System.out.println("Cart is empty");
        }
        System.out.println("===================\n");
    }
}
