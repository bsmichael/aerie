@notam
Feature: Notams

  @addEditPost
  Scenario: New members report
    Given I am an unauthenticated user
    When I add an edit post
    Then The request should be successful

  @getEditPost
  Scenario: New members report
    Given I am an unauthenticated user
    When I get an edit post
    Then The request should be successful
