@gatecode
Feature: Gate Codes

  Background:
    Given the roster database is up-to-date
    And a gate code has been set

  @add
  Scenario: Add a new gate code
    Given I am an unauthenticated user
    When I add a new gate code
    Then The request should be successful

  @getAll
  Scenario: Get all gate codes
    Given I am an unauthenticated user
    When I request all gate codes
    Then The request should be successful

  @slashCommand @current
  Scenario: Handle Slack /gatecode command for current members
    Given I am an unauthenticated user
    And my membership is current
    When I request the gate code via Slack
    Then The request should be successful
    And I should receive a gate code

  @slashCommand @family
  Scenario: Handle Slack /gatecode command for current family members
    Given I am an unauthenticated user
    And my membership is current
    And I am not the primary family member
    When I request the gate code via Slack
    Then The request should be successful
    And I should receive a gate code

  @slashCommand @expired
  Scenario: Handle Slack /gatecode command for expired members
    Given I am an unauthenticated user
    And my membership is expired
    When I request the gate code via Slack
    Then The request should be successful
    And I should receive a message with a link to renew my membership

  @slashCommand @unknown
  Scenario: Handle Slack /gatecode command for non-members
    Given I am an unauthenticated user
    And my membership is unknown
    When I request the gate code via Slack
    Then The request should be successful
    And I should receive a message with a link to become a member
