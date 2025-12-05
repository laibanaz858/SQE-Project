@checkout
Feature: Checkout Process

  Background:
    Given I am logged in successfully
    And I have items in my cart

  @smoke @hardcoded
  Scenario: Complete checkout process
    When I proceed to checkout
    And I fill checkout information
    And I complete purchase
    Then I should see order confirmation

  @excel @regression
  Scenario: Checkout with Excel test data
    When I checkout with Excel data for "Checkout1"
    Then order should be confirmed

  @database @regression
  Scenario: Checkout cancellation
    When I proceed to checkout
    And I cancel checkout
    Then I should return to cart page
    
  @redis @checkout
  Scenario: Cache checkout form data in Redis
    Given I am logged in successfully
    And I have items in my cart
    And Redis cache is connected
   When I fill checkout form with "Checkout1" test data
    And I cache checkout data in Redis
    Then I should retrieve same checkout data from Redis
    And proceed to complete purchase

  @redis @checkout @session
  Scenario: Resume interrupted checkout from Redis
    Given I started checkout process
    And checkout data is cached in Redis
    When my session times out
    And I login again
    And I load checkout data from Redis
    Then I should continue from where I left
    And complete the purchase