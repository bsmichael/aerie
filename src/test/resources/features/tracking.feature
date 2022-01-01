@tracking
Feature: Tracking

  @messageOpened
  Scenario: User opens a message that is being tracked
    Given I am an unauthenticated user
    And I have received an email message
    When I open a tracked message
    Then The message open event is recorded

  @all
  Scenario:
    Given I am an unauthenticated user
    When I request all tracking data
    Then The request should be successful
    And The response should have tracking data listed