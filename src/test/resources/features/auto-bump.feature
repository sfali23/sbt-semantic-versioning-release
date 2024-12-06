Feature: Branch with Auto bump repository

  Scenario: Create initial commit and create tag using startingVersion
    Given Current branch is 'main'
    When Make some changes
    And Commit with message: 'initial commit'
    And Create an annotated tag: true
    Then Generated version should be 'v0.1.0'

  Scenario: create branch and commit minor version, minor version should be bumped
    Given Branch 'test' is created and checked out
    When Make some changes
    And Commit with message: '[minor] version update'
    And Branch is checked out 'main'
    And Merge branch 'test' into current branch
    And Create an annotated tag: true
    Then Generated version should be 'v0.2.0'
