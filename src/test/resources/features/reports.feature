@reports
Feature: reports
  Membership reports

  @membershipreport @disabled
  Scenario: Retrieve membership report
    Given I am an unauthenticated user
    When I request the general membership report
    Then The request should be successful

  @fullmembershipreport @disabled
  Scenario: Retrieve membership report
    Given I am an unauthenticated user
    When I request the general full membership report
    Then The request should be successful
