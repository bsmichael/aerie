@admin
Feature: admin
  Various administrative functions

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

  @weather @update
  Scenario: Update weather data from AviationWeather.gov
    Given I am an unauthenticated user
    When I request the weather data to be updated
    Then The request should be successful

  @membership @auto @renewmember @disabled
  Scenario: Force run of automated send membership renewal messages
    Given I am an unauthenticated user
    When I request membership renewal messages be sent
    Then The request should be successful
