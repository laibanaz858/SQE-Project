package com.yourcompany.pages;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;

public class ProductsPage extends BasePage {
    
    @FindBy(className = "product_sort_container")
    private WebElement sortDropdown;
    
    @FindBy(className = "inventory_item")
    private List<WebElement> productItems;
    
    @FindBy(className = "btn_inventory")
    private List<WebElement> addToCartButtons;
    
    @FindBy(className = "inventory_item_name")
    private List<WebElement> productNames;
    
    @FindBy(className = "inventory_item_price")
    private List<WebElement> productPrices;
    
    @FindBy(className = "title")
    private WebElement pageTitle;
    
    public ProductsPage(WebDriver driver) {
        super(driver);
    }
    
    public void sortProducts(String sortOption) {
        try {
            System.out.println("Sorting by: " + sortOption);
            waitForPageToLoad();
            sleep(1000);
            
            if (!isProductsPageDisplayed()) {
                System.out.println("Not on products page! Current URL: " + driver.getCurrentUrl());
                driver.get("https://www.saucedemo.com/inventory.html");
                waitForPageToLoad();
                sleep(2000);
            }
            
            System.out.println("Page Title: " + (pageTitle.isDisplayed() ? pageTitle.getText() : "Not found"));
            System.out.println("Sort dropdown displayed: " + sortDropdown.isDisplayed());
            
            if (sortOption == null || sortOption.trim().isEmpty()) {
                sortOption = "Name (A to Z)";
            }
            
            waitForElement(sortDropdown);
            Select select = new Select(sortDropdown);
            String dropdownValue = getDropdownValue(sortOption);
            
            System.out.println("Selecting dropdown value: " + dropdownValue);
            select.selectByValue(dropdownValue);
            
            sleep(2000);
            System.out.println("Products sorted");
            
        } catch (Exception e) {
            System.out.println("Sorting error: " + e.getMessage());
            sortProductsWithJS(sortOption);
        }
    }
    
    private void sortProductsWithJS(String sortOption) {
        try {
            System.out.println("Trying JS sorting for: " + sortOption);
            String dropdownValue = getDropdownValue(sortOption);
            String js = "document.querySelector('.product_sort_container').value = '" + dropdownValue + "';" +
                       "document.querySelector('.product_sort_container').dispatchEvent(new Event('change'));";
            this.js.executeScript(js);
            
            sleep(2000);
            System.out.println("Products sorted with JS");
            
        } catch (Exception e) {
            System.out.println("JS sorting also failed: " + e.getMessage());
            throw e;
        }
    }
    
    private String getDropdownValue(String sortOption) {
        if (sortOption == null) return "az";
        
        String lowerOption = sortOption.toLowerCase();
        if (lowerOption.contains("name (a to z)") || lowerOption.contains("alphabetical ascending")) {
            return "az";
        } else if (lowerOption.contains("name (z to a)") || lowerOption.contains("alphabetical descending")) {
            return "za";
        } else if (lowerOption.contains("price (low to high)") || lowerOption.contains("price ascending")) {
            return "lohi";
        } else if (lowerOption.contains("price (high to low)") || lowerOption.contains("price descending")) {
            return "hilo";
        } else {
            return "az";
        }
    }
    
    public boolean verifyPriceSorting(boolean ascending) {
        try {
            sleep(2000);
            List<Double> prices = getPriceValues();
            System.out.println("Found " + prices.size() + " products");
            if (prices.size() < 2) return true;
            
            for (int i = 0; i < prices.size() - 1; i++) {
                if (ascending) {
                    if (prices.get(i) > prices.get(i + 1)) return false;
                } else {
                    if (prices.get(i) < prices.get(i + 1)) return false;
                }
            }
            return true;
            
        } catch (Exception e) {
            System.out.println("Price verification error: " + e.getMessage());
            return false;
        }
    }
    
    public boolean verifyAlphabeticalSorting(boolean ascending) {
        try {
            sleep(2000);
            List<String> names = getProductNames();
            System.out.println("Found " + names.size() + " products");
            if (names.size() < 2) return true;
            
            for (int i = 0; i < names.size() - 1; i++) {
                int comparison = names.get(i).compareToIgnoreCase(names.get(i + 1));
                if (ascending) {
                    if (comparison > 0) return false;
                } else {
                    if (comparison < 0) return false;
                }
            }
            return true;
            
        } catch (Exception e) {
            System.out.println("Alphabetical verification error: " + e.getMessage());
            return false;
        }
    }
    
    public void addItemToCart(int itemIndex) {
        try {
            sleep(1000);
            if (itemIndex >= 0 && itemIndex < addToCartButtons.size()) {
                System.out.println("Adding item at index: " + itemIndex);
                WebElement addButton = addToCartButtons.get(itemIndex);
                System.out.println("Button text before: " + addButton.getText());
                js.executeScript("arguments[0].click();", addButton);
                sleep(1000);
                System.out.println("Button text after: " + addButton.getText());
                System.out.println("Added item " + (itemIndex + 1) + " to cart");
            } else {
                System.out.println("Invalid item index: " + itemIndex);
            }
        } catch (Exception e) {
            System.out.println("Error adding item to cart: " + e.getMessage());
        }
    }
    
    public void addMultipleItemsToCart(int count) {
        try {
            System.out.println("Adding " + count + " items to cart");
            for (int i = 0; i < count && i < addToCartButtons.size(); i++) {
                addItemToCart(i);
            }
            System.out.println("Added " + count + " items to cart");
        } catch (Exception e) {
            System.out.println("Error adding multiple items: " + e.getMessage());
        }
    }
    
    public List<String> getProductNames() {
        List<String> names = new ArrayList<>();
        try {
            sleep(1000);
            for (WebElement element : productNames) {
                names.add(element.getText());
            }
        } catch (Exception e) {
            System.out.println("Error getting product names: " + e.getMessage());
        }
        return names;
    }
    
    public List<Double> getPriceValues() {
        List<Double> prices = new ArrayList<>();
        try {
            sleep(1000);
            for (WebElement element : productPrices) {
                prices.add(extractPrice(element.getText()));
            }
        } catch (Exception e) {
            System.out.println("Error getting prices: " + e.getMessage());
        }
        return prices;
    }
    
    public List<String> getProductPrices() {
        List<String> prices = new ArrayList<>();
        try {
            sleep(1000);
            for (WebElement element : productPrices) {
                prices.add(element.getText());
            }
        } catch (Exception e) {
            System.out.println("Error getting price strings: " + e.getMessage());
        }
        return prices;
    }
    
    public int getProductCount() {
        return productItems.size();
    }
    
    private double extractPrice(String priceText) {
        try {
            return Double.parseDouble(priceText.replace("$", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    public void printProductsInfo() {
        System.out.println("\nPRODUCTS INFO");
        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("Total products: " + getProductCount());
        System.out.println("Products page displayed: " + isProductsPageDisplayed());
        
        List<String> names = getProductNames();
        List<String> prices = getProductPrices();
        
        if (!names.isEmpty()) {
            System.out.println("Product List:");
            for (int i = 0; i < Math.min(names.size(), 5); i++) {
                System.out.println("  " + (i + 1) + ". " + names.get(i) + " - " + prices.get(i));
            }
        }
        System.out.println("=====================\n");
    }
    
    public boolean isProductsPageDisplayed() {
        try {
            Thread.sleep(1000);
            boolean onProductsPage = driver.getCurrentUrl().contains("inventory");
            System.out.println("Products Page Check:");
            System.out.println("  - Current URL: " + driver.getCurrentUrl());
            System.out.println("  - Contains 'inventory': " + onProductsPage);
            return onProductsPage;
        } catch (Exception e) {
            System.out.println("Error in isProductsPageDisplayed: " + e.getMessage());
            return false;
        }
    }
}
