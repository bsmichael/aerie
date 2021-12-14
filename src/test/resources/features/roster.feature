@roster
Feature: Roster functions

  @update
  Scenario: Update roster data from roster management system
    Given I am an unauthenticated user
    When I request an update of the roster data
    Then The request should be successful

  @report @disabled
  Scenario: Retrieve roster membership report
    Given I am an unauthenticated user
    When I request the roster membership report
    Then The request should be successful

  @expiration
  Scenario: Retrieve member expiration
    Given I am an unauthenticated user
    When I request the expiration data for member with ID 42648
    Then The request should be successful

  @rfid
  Scenario: Retrieve all member's RFID data
    Given I am an unauthenticated user
    When I request RFID data for all members
    Then The request should be successful

  @rfid @update
  Scenario: Update a member's RFID data
    Given I am an unauthenticated user
    When I update member 42648's RFID with ABC123
    Then The request should be successful

  @rfid @findByID
  Scenario: Find a member by their RFID data
    Given I am an unauthenticated user
    When I find a member by their RFID ABC123
    Then The request should be successful
