@reports
Feature: reports
  Membership reports

  @membershipreport
  Scenario: Retrieve membership report
    Given I am an unauthenticated user
    When I request the general membership report
    Then The request should be successful

  @fullmembershipreport
  Scenario: Retrieve membership report
    Given I am an unauthenticated user
    When I request the general full membership report
    Then The request should be successful
