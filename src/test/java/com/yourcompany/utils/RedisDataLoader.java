package com.yourcompany.utils;

import redis.clients.jedis.Jedis;

public class RedisDataLoader {
    
    public static void main(String[] args) {
        System.out.println("=== Loading Test Data into Redis ===");
        
        Jedis jedis = new Jedis("localhost", 6379);
        
        jedis.del("LoginData:*");
        jedis.del("CartData:*");
        jedis.del("CheckoutData:*");
        jedis.del("SortData:*");
        
        jedis.set("LoginData:ValidUser:Username", "standard_user");
        jedis.set("LoginData:ValidUser:Password", "secret_sauce");
        jedis.set("LoginData:ValidUser:Expected", "success");
        
        jedis.set("LoginData:InvalidUser:Username", "invalid_user");
        jedis.set("LoginData:InvalidUser:Password", "wrong_pass");
        jedis.set("LoginData:InvalidUser:Expected", "error");
        
        jedis.set("CartData:CartItems:ItemCount", "3");
        jedis.set("CartData:CartItems:ExpectedCount", "3");
        
        jedis.set("CartData:CartCache:ItemCount", "2");
        jedis.set("CartData:CartCache:ExpectedCount", "2");
        
        jedis.set("CheckoutData:CheckoutTest:FirstName", "John");
        jedis.set("CheckoutData:CheckoutTest:LastName", "Doe");
        jedis.set("CheckoutData:CheckoutTest:PostalCode", "12345");
        
        jedis.set("CheckoutData:AdminCheckout:FirstName", "Jane");
        jedis.set("CheckoutData:AdminCheckout:LastName", "Smith");
        jedis.set("CheckoutData:AdminCheckout:PostalCode", "67890");
        
        jedis.set("SortData:PriceFilter:Filter", "Price (high to low)");
        jedis.set("SortData:SortPreference:Filter", "Name (A to Z)");
        
        System.out.println("Test data loaded successfully!");
        System.out.println("Total keys in Redis: " + jedis.dbSize());
        
        System.out.println("Sample Data:");
        System.out.println("Login username: " + jedis.get("LoginData:ValidUser:Username"));
        System.out.println("Cart count: " + jedis.get("CartData:CartItems:ItemCount"));
        System.out.println("Checkout name: " + jedis.get("CheckoutData:CheckoutTest:FirstName"));
        System.out.println("Sort filter: " + jedis.get("SortData:PriceFilter:Filter"));
        
        jedis.close();
    }
}