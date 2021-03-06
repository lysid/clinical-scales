Feature: The Indicator aggregate
  
  Test the business rules and invariants for the Indicator aggregate.
  
  
  Scenario: An Indicator is created in Draft state
    When I create a new Indicator
    Then its status should be Draft
  
  
  Scenario: Publishing an Indicator changes its state into Published
    Given a Draft Indicator
    When I publish this Indicator
    Then its status should be Published
  
  
  Scenario Outline: An Indicator can only be published when in Draft state
    Given a <initialState> Indicator
    When I publish this Indicator
    Then it should fail with message "An indicator can be published only when it has a Draft status."
    
    Examples:
      | initialState |
      | Published    |
      | Archived     |
      | Evolved      |
  
  
  Scenario Outline: Archiving an Indicator changes its state into Archived
    Given a <initialState> Indicator
    When I archive this Indicator
    Then its status should be Archived
    
    Examples:
      | initialState |
      | Draft        |
      | Published    |
      | Evolved      |
  
  
  Scenario: An Archived Indicator cannot be archived again
    Given an Archived Indicator
    When I archive this Indicator
    Then it should fail with message "An indicator can be archived only when it has a Draft, Published or Evolved status."
  
  
  Scenario: An Indicator can be archived if it is used by one Scale that is also archived
    Given a Published Indicator
    And an Archived Scale using this Indicator
    When I archive this Indicator
    Then the Indicator status should be Archived
  
  
  Scenario: An Indicator can be archived if it is used by several Scales that are also archived
    Given a Published Indicator
    And an Archived Scale using this Indicator
    And another Archived Scale using this Indicator
    When I archive this Indicator
    Then the Indicator status should be Archived
  
  
  Scenario Outline: An Indicator cannot be archived if a Scale using it is not archived
    Given a Published Indicator
    And a <scaleStatus> Scale using this Indicator
    When I archive this Indicator
    Then it should fail with message "An indicator can be archived only if no Scale is using it or if all Scales using it are also archived."
    
    Examples:
      | scaleStatus |
      | Draft       |
      | Published   |
      | Evolved     |
  
  
  Scenario Outline: An Indicator cannot be archived if one of several Scales using it is not archived
    Given a Published Indicator
    And a <firstScaleStatus> Scale using this Indicator
    And another <secondScaleStatus> Scale using this Indicator
    When I archive this Indicator
    Then it should fail with message "An indicator can be archived only if no Scale is using it or if all Scales using it are also archived."
    
    Examples:
      | firstScaleStatus | secondScaleStatus |
      | Draft            | Draft             |
      | Draft            | Published         |
      | Draft            | Archived          |
      | Draft            | Evolved           |
      | Published        | Draft             |
      | Published        | Published         |
      | Published        | Archived          |
      | Published        | Evolved           |
      | Archived         | Draft             |
      | Archived         | Published         |
      | Archived         | Evolved           |
      | Evolved          | Draft             |
      | Evolved          | Published         |
      | Evolved          | Archived          |
      | Evolved          | Evolved           |
    
    
  Scenario Outline: Unarchiving an Indicator changes its state back into its original state
    Given an Archived Indicator previously in <previousState> state
    When I unarchive this Indicator
    Then its status should be <previousState>
  
    Examples:
    | previousState |
    | Draft         |
    | Published     |
    | Evolved       |
    
  
  Scenario Outline: Only Archived Indicator can be unarchived
    Given an <initialState> Indicator
    When I unarchive this Indicator
    Then it should fail with message "An indicator can be unarchived only when it has an Archived status."
  
    Examples:
      | initialState |
      | Draft        |
      | Published    |
      | Evolved      |
  
  
  Scenario: Evolving an Indicator into another one
    Given a Published Indicator
    And another Draft Indicator
    When I evolve one into the other
    Then the former Indicator status should be Evolved
    And it should designate the latter as its evolved version