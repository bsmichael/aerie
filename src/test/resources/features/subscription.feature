@subscription
Feature: Subscription

  @unsubscribe
  Scenario: User unsubscribes from emails
    Given I am an unauthenticated user
    And I have received an email message
    And I click on the unsubscribe link
    When I confirm I want to unsubscribe from email messages
    Then I have email disabled

  @subscribe
  Scenario: User subscribes to emails
    Given I am an unauthenticated user
    And I want to subscribe to email messages
    When I confirm I want to subscribe to email messages
    Then I have email enabled

  @resubscribe
  Scenario: User resubscribes to emails
    Given I am an unauthenticated user
    And I want to resubscribe to email messages
    When I confirm I want to subscribe to email messages
    Then I have email enabled
