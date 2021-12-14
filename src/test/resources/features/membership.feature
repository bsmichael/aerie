@membership
Feature: membership
  Membership features

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
