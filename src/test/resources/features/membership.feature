@membership
Feature: membership
  Membership features

  @email @newmember @disabled
  Scenario: Send new membership email
    Given I am an unauthenticated user
    When I request an email be sent to new member 42648
    Then The request should be successful

  @email @renewmember @disabled
  Scenario: Send renew membership email
    Given I am an unauthenticated user
    When I request a message be sent to member 42648 to renew their membership
    Then The request should be successful

  @mailchimp @member @disabled
  Scenario: Add a new member to MailChimp with an invalid rosterId
    Given I am an unauthenticated user
    When I request member 42648 be added to the MailChimp member list
    Then The request should be successful

  @mailchimp @nonmember @disabled
  Scenario: Add a non member to MailChimp with an invalid rosterId
    Given I am an unauthenticated user
    When I request non-member 42648 be added to the MailChimp non-member list
    Then The request should be successful

  @email @newmember @invaliddata @disabled
  Scenario: Send new membership email to an invalid member
    Given I am an unauthenticated user
    When I request an email be sent to new member 0
    Then A not found exception should be thrown

  @mailchimp @member @invaliddata @disabled
  Scenario: Add a new member to MailChimp with an invalid rosterId
    Given I am an unauthenticated user
    When I request member 0 be added to the MailChimp member list
    Then A not found exception should be thrown

  @mailchimp @nonmember @invaliddata @disabled
  Scenario: Add a non member to MailChimp with an invalid rosterId
    Given I am an unauthenticated user
    When I request non-member 0 be added to the MailChimp non-member list
    Then A not found exception should be thrown

  @membership @auto @renewmember @disabled
  Scenario: Force run of automated send membership renewal messages
    Given I am an unauthenticated user
    When I request membership renewal messages be sent
    Then The request should be successful

  @newmember @disabled
  Scenario: New chapter member providing only a cell phone number as a contact method.
    Given I am a new chapter member
    And I do not provide an email address
    And I provide a cell phone number
    And I do not provide a Slack name
    When I submit a new membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from now
    And I should not receive a new member welcome email message
    And I should receive a new member SMS/Text message
    And I should not receive a new member Slack message

  @newmember @disabled
  Scenario: New chapter member providing only an email address as a contact method.
    Given I am a new chapter member
    And I provide an email address
    And I do not provide a cell phone number
    And I do not provide a Slack name
    When I submit a new membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from now
    And I should receive a new member welcome email message
    And I should not receive a new member SMS/Text message
    And I should not receive a new member Slack message

  @renewingmember @disabled
  Scenario: Existing chapter member submits a new membership Jot Form.
    Given I have a record in the roster management system
    And I have enabled sending of messages by email
    When I submit a new membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from my previous expiration date
    And I should receive a renew member welcome email message

  @renewingmember @disabled
  Scenario: Renewing chapter member providing all contact methods.
    Given I am a chapter member
    And I have enabled sending of messages by email
    And I have enabled sending of messages by SMS/Text
    And I have enabled sending of messages by Slack
    When I submit a renew membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from my previous expiration date
    And I should receive a new member welcome email message
    And I should receive a new member SMS/Text message
    And I should receive a new member Slack message

  @renewingmember @disabled
  Scenario: Existing chapter member providing only an email address as a contact method.
    Given I am a chapter member
    And I have enabled sending of messages by email
    And I have disabled sending of messages by SMS/Text
    And I have disabled sending of messages by Slack
    When I submit a renew membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from my previous expiration date
    And I should receive a new member welcome email message
    And I should not receive a new member SMS/Text message
    And I should not receive a new member Slack message

  @newmember @disabled
  Scenario: New member submits a renew membership Jot Form.
    Given I am a new chapter member
    And I do not have a record in the roster management system
    And I provide an email address
    And I do not provide a cell phone number
    And I do not provide a Slack name
    When I submit a renew membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from now
    And I should receive a new member welcome email message
    And I should not receive a new member SMS/Text message
    And I should not receive a new member Slack message

  @status @disabled
  Scenario: Chapter member checks their membership status
    Given I am a chapter member
    When I check my membership status
    Then I should receive my membership details

  @status
  Scenario: Non-member checks their membership status
    Given I am not a chapter member
    When I check my membership status
    Then A bad request exception should be thrown

  @jotform @update
  Scenario: Aerie checks for any new JotForm submissions
    When Aerie checks for JotForm submissions
    Then The request should be successful