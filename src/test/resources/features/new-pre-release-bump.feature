Feature: New pre-release bumping scenarios

  @new-pre-release
  Scenario Outline: New pre-release without any previous pre-release version will create new pre-release (Force bump)
    Given Current branch is 'main'
    And Load semantic build config from ({forceBump=true, componentToBump=<bumpComponent>,newPreRelease=true,snapshot=<snapshotFlag>})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'new pre-release creation'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion         | snapshotFlag  |
      | PATCH         | true      | 0.1.1-RC.1              | false         |
      | PATCH         | false     | 0.1.1-RC.1              | false         |
      | PATCH         | true      | 0.1.1-RC.1-SNAPSHOT     | true          |
      | PATCH         | false     | 0.1.1-RC.1-SNAPSHOT     | true          |
      | MINOR         | true      | 0.2.0-RC.1              | false         |
      | MINOR         | false     | 0.2.0-RC.1              | false         |
      | MINOR         | true      | 0.2.0-RC.1-SNAPSHOT     | true          |
      | MINOR         | false     | 0.2.0-RC.1-SNAPSHOT     | true          |
      | MAJOR         | true      | 1.0.0-RC.1              | false         |
      | MAJOR         | false     | 1.0.0-RC.1              | false         |
      | MAJOR         | true      | 1.0.0-RC.1-SNAPSHOT     | true          |
      | MAJOR         | false     | 1.0.0-RC.1-SNAPSHOT     | true          |

  @new-pre-release
  Scenario Outline: With previous pre-release version will bump pre-release version (Force bump)
    Given Current branch is 'main'
    And Load semantic build config from ({forceBump=true, snapshot=<snapshotFlag>})
    And Following annotated: <annotated> tags (v0.1.0,<previousVersion>) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'bump pre-release version'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | annotated | previousVersion          | expectedVersion     | snapshotFlag  |
      | true      | v0.1.1-RC.1              | 0.1.1-RC.2          | false         |
      | false     | v0.1.1-RC.1              | 0.1.1-RC.2          | false         |
      | true      | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-RC.2-SNAPSHOT | true          |
      | false     | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-RC.2-SNAPSHOT | true          |
      | true      | v0.2.0-RC.1              | 0.2.0-RC.2          | false         |
      | false     | v0.2.0-RC.1              | 0.2.0-RC.2          | false         |
      | true      | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-RC.2-SNAPSHOT | true          |
      | false     | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-RC.2-SNAPSHOT | true          |
      | true      | v1.0.0-RC.1              | 1.0.0-RC.2          | false         |
      | false     | v1.0.0-RC.1              | 1.0.0-RC.2          | false         |
      | true      | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-RC.2-SNAPSHOT | true          |
      | false     | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-RC.2-SNAPSHOT | true          |

  @new-pre-release
  Scenario Outline: With current version is pre-release, promote to release (Force bump)
    Given Current branch is 'main'
    And Load semantic build config from ({forceBump=true, promoteToRelease=true, snapshot=<snapshotFlag>})
    And Following annotated: <annotated> tags (v0.1.0,<previousVersion>) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'promote to release'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | annotated | previousVersion          | expectedVersion     | snapshotFlag  |
      | true      | v0.1.1-RC.1              | 0.1.1               | false         |
      | false     | v0.1.1-RC.1              | 0.1.1               | false         |
      | true      | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-SNAPSHOT      | true          |
      | false     | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-SNAPSHOT      | true          |
      | true      | v0.2.0-RC.1              | 0.2.0               | false         |
      | false     | v0.2.0-RC.1              | 0.2.0               | false         |
      | true      | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-SNAPSHOT      | true          |
      | false     | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-SNAPSHOT      | true          |
      | true      | v1.0.0-RC.1              | 1.0.0               | false         |
      | false     | v1.0.0-RC.1              | 1.0.0               | false         |
      | true      | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-SNAPSHOT      | true          |
      | false     | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-SNAPSHOT      | true          |

  @new-pre-release
  Scenario Outline: New pre-release without any previous pre-release version will create new pre-release (Auto bump)
    Given Current branch is 'main'
    And Load semantic build config from ({snapshot=<snapshotFlag>})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '[new-pre-release] creation with [<bumpComponent>]'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion         | snapshotFlag  |
      | patch         | true      | 0.1.1-RC.1              | false         |
      | patch         | false     | 0.1.1-RC.1              | false         |
      | patch         | true      | 0.1.1-RC.1-SNAPSHOT     | true          |
      | patch         | false     | 0.1.1-RC.1-SNAPSHOT     | true          |
      | minor         | true      | 0.2.0-RC.1              | false         |
      | minor         | false     | 0.2.0-RC.1              | false         |
      | minor         | true      | 0.2.0-RC.1-SNAPSHOT     | true          |
      | minor         | false     | 0.2.0-RC.1-SNAPSHOT     | true          |
      | major         | true      | 1.0.0-RC.1              | false         |
      | major         | false     | 1.0.0-RC.1              | false         |
      | major         | true      | 1.0.0-RC.1-SNAPSHOT     | true          |
      | major         | false     | 1.0.0-RC.1-SNAPSHOT     | true          |

  @new-pre-release
  Scenario Outline: With previous pre-release version will bump pre-release version (Auto bump)
    Given Current branch is 'main'
    And Load semantic build config from ({snapshot=<snapshotFlag>})
    And Following annotated: <annotated> tags (v0.1.0,<previousVersion>) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'bump pre-release version, [MAJOR] should be ignored'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | annotated | previousVersion          | expectedVersion      | snapshotFlag  |
      | true      | v0.1.1-RC.1              | 0.1.1-RC.2           | false         |
      | false     | v0.1.1-RC.1              | 0.1.1-RC.2           | false         |
      | true      | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-RC.2-SNAPSHOT  | true          |
      | false     | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-RC.2-SNAPSHOT  | true          |
      | true      | v0.2.0-RC.1              | 0.2.0-RC.2           | false         |
      | false     | v0.2.0-RC.1              |0.2.0-RC.2            | false         |
      | true      | v0.2.0-RC.1-SNAPSHOT     |0.2.0-RC.2-SNAPSHOT   | true          |
      | false     | v0.2.0-RC.1-SNAPSHOT     |0.2.0-RC.2-SNAPSHOT   | true          |
      | true      | v1.0.0-RC.1              |1.0.0-RC.2            | false         |
      | false     | v1.0.0-RC.1              |1.0.0-RC.2            | false         |
      | true      | v1.0.0-RC.1-SNAPSHOT     |1.0.0-RC.2-SNAPSHOT   | true          |
      | false     | v1.0.0-RC.1-SNAPSHOT     |1.0.0-RC.2-SNAPSHOT   | true          |

  @new-pre-release
  Scenario Outline: With current version is pre-release, promote to release (Auto bump)
    Given Current branch is 'main'
    And Load semantic build config from ({snapshot=<snapshotFlag>})
    And Following annotated: <annotated> tags (v0.1.0,<previousVersion>) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '[promote] to release, [MINOR] should be ignored'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | annotated | previousVersion          | expectedVersion     | snapshotFlag  |
      | true      | v0.1.1-RC.1              | 0.1.1               | false         |
      | false     | v0.1.1-RC.1              | 0.1.1               | false         |
      | true      | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-SNAPSHOT      | true          |
      | false     | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-SNAPSHOT      | true          |
      | true      | v0.2.0-RC.1              | 0.2.0               | false         |
      | false     | v0.2.0-RC.1              | 0.2.0               | false         |
      | true      | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-SNAPSHOT      | true          |
      | false     | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-SNAPSHOT      | true          |
      | true      | v1.0.0-RC.1              | 1.0.0               | false         |
      | false     | v1.0.0-RC.1              | 1.0.0               | false         |
      | true      | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-SNAPSHOT      | true          |
      | false     | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-SNAPSHOT      | true          |
