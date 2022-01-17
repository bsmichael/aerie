@roster
Feature: Roster functions

  @update
  Scenario: Update roster data from roster management system
    Given I am an unauthenticated user
    When I request an update of the roster data
    Then The request should be successful

  @update
  Scenario: Update roster data from roster management system (2nd iteration)
    Given I am an unauthenticated user
    When I request an update of the roster data
    Then The request should be successful

  @email @renewmember
  Scenario: Send renew membership email
    Given I am an unauthenticated user
    And email is enabled
    And slack is disabled
    When I request a message be sent to member 42648 to renew their membership
    Then The request should be successful

  @email @renewmember
  Scenario: Send renew membership email with services disabled
    Given I am an unauthenticated user
    And email is disabled
    And slack is disabled
    When I request a message be sent to member 42648 to renew their membership
    Then The request should be successful

  @email @renewmember
  Scenario: Send renew membership email to a member with no email address
    Given I am an unauthenticated user
    And email is enabled
    When I request a message be sent to member 67972 to renew their membership
    Then The request should be successful

  @email @renewmember
  Scenario: Send renew membership email to a non-existant member
    Given I am an unauthenticated user
    And email is enabled
    When I request a message be sent to member 0 to renew their membership
    Then A not found exception should be thrown

  @status
  Scenario: Non-member checks their membership status
    Given I am not a chapter member
    When I check membership information for 0
    Then A not found exception should be thrown

  @jotform @update
  Scenario: Aerie checks for any new JotForm submissions
    When Aerie checks for JotForm submissions
    Then The request should be successful

  @rfid
  Scenario: Retrieve all member's RFID data
    Given I am an unauthenticated user
    When I request RFID data for all members
    Then The request should be successful

  @membershipreport
  Scenario: Retrieve membership report
    Given I am an unauthenticated user
    When I request a membership report
    Then The request should be successful

  @rfid @findByID
  Scenario: Find a member by their RFID data
    Given I am an unauthenticated user
    When I find a member by their RFID ABC123
    Then The request should be successful

  @rfid @findByID
  Scenario: Find a member by an invalid RFID
    Given I am an unauthenticated user
    When I find a member by their RFID INVALID
    Then A not found exception should be thrown

  @rfid @update
  Scenario: Update a member's RFID data
    Given I am an unauthenticated user
    When I update member 67972's RFID with ABC123
    Then The request should be successful

  @status
  Scenario: Chapter member checks their membership information
    Given I am a chapter member
    When I check membership information for 42648
    Then I should receive my membership details

  @expiration
  Scenario: Retrieve member expiration
    Given I am an unauthenticated user
    When I request the expiration data for member with ID 42648
    Then The request should be successful

  @membership @auto @renewmember
  Scenario: Force run of automated send membership renewal messages
    Given I am an unauthenticated user
    And email is disabled
    And slack is disabled
    When I request membership renewal messages be sent
    Then The request should be successful

  @email @newmember
  Scenario: Send new membership email
    Given I am an unauthenticated user
    And email is enabled
    When I request an email be sent to new member 42648
    Then The request should be successful

  @email @newmember @invaliddata
  Scenario: Send new membership email to an invalid member
    Given I am an unauthenticated user
    And email is enabled
    When I request an email be sent to new member 0
    Then A not found exception should be thrown

  @findByName
  Scenario Outline:
    Given I am an unauthenticated user
    When I find by name members with <firstName> and <lastName>
    Then The request should be successful
    And I should receive a list of membership details with <name>

    Examples:
    | firstName | lastName | name          |
    | Brian     | Michael  | Brian Michael |
    | Brian     | null     | Brian Michael |
    | null      | Michael  | Brian Michael |
    | null      | null     | null          |

  @search
  Scenario Outline:
    Given I am an unauthenticated user
    When I search for members with <firstName> and <lastName>
    Then The request should be successful

    Examples:
      | firstName | lastName |
      | Brian     | Michael  |
      | Brian     | null     |
      | null      | Michael  |
      | null      | null     |

  @slack
  Scenario: Get all Slack users
    Given I am an unauthenticated user
    When I request all Slack users
    Then The request should be successful
    And The response should have Slack users listed

  @slashCommand
  Scenario: Handle Slack /membership command for members
    Given I am an unauthenticated user
    And my membership is current
    When I request my membership info via Slack
    Then The request should be successful
    And I should receive a membership info message

  @slashCommand @family
  Scenario: Handle Slack /membership command for family members
    Given I am an unauthenticated user
    And my membership is current
    And I am not the primary family member
    When I request my membership info via Slack
    Then The request should be successful
    And I should receive a membership info message

  @slashCommand
  Scenario: Handle Slack /membership command for non-members
    Given I am an unauthenticated user
    And my membership is unknown
    When I request my membership info via Slack
    Then The request should be successful
    And I should receive a message with a link to become a member

  @newmember @disabled
  Scenario: New chapter member
    Given I am a new chapter member
    When I submit a new membership Jot Form
    Then I should have a record in the roster management system
    And my membership expiration should be set to 1 year from now

  @renewingmember @disabled
  Scenario: Existing chapter member submits a new membership Jot Form.
    Given I have a record in the roster management system
    When I submit a new membership Jot Form
    Then I should have a record in the roster management system
    And my membership expiration should be set to 1 year from my previous expiration date
    And I should receive a renew member welcome email message

  @renewingmember @disabled
  Scenario: Renewing chapter member submits a renew membership JotForm.
    Given I am a chapter member
    When I submit a renew membership Jot Form
    Then I should have a record in the roster management system
    And my membership expiration should be set to 1 year from my previous expiration date

  @newmember @disabled
  Scenario: New member submits a renew membership Jot Form.
    Given I am a new chapter member
    And I do not have a record in the roster management system
    When I submit a renew membership Jot Form
    Then I should have a record in the roster management system
    And my membership expiration should be set to 1 year from now
