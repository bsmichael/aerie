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

  @membership
  Scenario: Retrieve membership report
    Given I am an unauthenticated user
    When I request a membership report
    Then The request should be successful