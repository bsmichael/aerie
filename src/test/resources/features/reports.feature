@report
Feature: Reports

  @expiring
  Scenario: Expiring report
    Given I am an unauthenticated user
    When I request a membership expiring report
    Then The request should be successful

  @expired
  Scenario: Expired report
    Given I am an unauthenticated user
    When I request a membership expired report
    Then The request should be successful

  @new
  Scenario: New members report
    Given I am an unauthenticated user
    When I request a new members report
    Then The request should be successful

  @current
  Scenario: Current members report
    Given I am an unauthenticated user
    When I request a current members report
    Then The request should be successful

  @current
  Scenario: Non-national members report
    Given I am an unauthenticated user
    When I request a non-national members report
    Then The request should be successful

  @membership
  Scenario: Retrieve membership report
    Given I am an unauthenticated user
    When I request a membership report
    Then The request should be successful