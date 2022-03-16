@rover
Feature: M.A.R.S. Rover

  @dashboard
  Scenario: Get rover dashboard
    Given I am an unauthenticated user
    When I request the rover dashboard
    Then The request should be successful

  @addTeam
  Scenario: Add a new rover team
    Given I am an unauthenticated user
    When I add a new rover team
    Then The request should be successful
    And I should receive a rover team id

  @getAllTeams
  Scenario: Get all rover teams
    Given I am an unauthenticated user
    When I request all rover teams
    Then The request should be successful

  @getTeam
  Scenario: Get a rover team
    Given I am an unauthenticated user
    And A rover team exists
    When I request a rover team
    Then The request should be successful

  @getLatestTeamImage
  Scenario: Get the latest rover team image
    Given I am an unauthenticated user
    And A rover team exists
    When I request a rover team's latest image
    Then The request should be successful

  @postNewImage
  Scenario: Post a new image for a rover team
    Given I am an unauthenticated user
    And A rover team exists
    When I post a new image for a rover team
    Then The request should be successful
