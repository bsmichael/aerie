@groundschool
Feature: GroundSchool

  Background:
    Given the question database is up-to-date

  @getQuestion
  Scenario Outline: Get a question
    Given I am an unauthenticated user
    When I request question <RemoteQuestionID> in the <Course> course
    Then The request should be successful

    Examples:
      | RemoteQuestionID | Course |
      | 1920             | PVT    |
      | 324              | IFR    |
      | 97               | COM    |
      | 5679             | AMA    |
      | 6832             | AMG    |
      | 5644             | AMP    |
      | 2162             | ATP    |
      | 1058             | CFI    |
      | 653              | FLE    |
      | 3286             | IOF    |
      | 123              | MCI    |
      | 71               | MIL    |
      | 9857             | PAR    |
      | 565              | RDP    |
      | 429              | SPG    |
      | 455              | SPI    |

  @getAnswers
  Scenario Outline: Get answers for a question
    Given I am an unauthenticated user
    When I request the answers for question <RemoteQuestionID> in the <Course> course
    Then The request should be successful

    Examples:
      | RemoteQuestionID | Course |
      | 1920             | PVT    |
      | 324              | IFR    |
      | 97               | COM    |
      | 5679             | AMA    |
      | 6832             | AMG    |
      | 5644             | AMP    |
      | 2162             | ATP    |
      | 1058             | CFI    |
      | 653              | FLE    |
      | 3286             | IOF    |
      | 123              | MCI    |
      | 71               | MIL    |
      | 9857             | PAR    |
      | 565              | RDP    |
      | 429              | SPG    |
      | 455              | SPI    |

  @getQuestion
  Scenario: Get a question from an invalid course
    Given I am an unauthenticated user
    When I request question 1920 in the BSM course
    Then A not found exception should be thrown

  @getAnswers
  Scenario: Get answers for a question in an invalid course
    Given I am an unauthenticated user
    When I request the answers for question 1920 in the BSM course
    Then A not found exception should be thrown
