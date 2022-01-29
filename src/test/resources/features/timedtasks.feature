@gatecode
Feature: Gate Codes

  @executeTask
  Scenario Outline: Trigger execution of a task
    Given I am an unauthenticated user
    And email is disabled
    And slack is disabled
    When I trigger the execution of the <taskId> task
    Then The request should be successful

    Examples:
      | taskId                           |
      | update-weather                   |
      | update-roster                    |
      | get-jot-form-submissions         |
      | send-membership-renewal-messages |
      | clean-job-status-repo            |

  @status
  Scenario Outline: Checks the status of a task
    Given I am an unauthenticated user
    And email is disabled
    And slack is disabled
    And the <taskId> task has recently completed
    When I check the status of the <taskId> task
    Then The request should be successful

    Examples:
      | taskId                           |
      | update-weather                   |
      | update-roster                    |
      | get-jot-form-submissions         |
      | send-membership-renewal-messages |
      | clean-job-status-repo            |
