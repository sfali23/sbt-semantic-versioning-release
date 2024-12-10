Feature: New pre-release bumping scenarios

  @new-pre-release
  Scenario Outline: New pre-release without any previous pre-release version will create new pre-release (Force bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (<bumpComponent>,newPreRelease,<snapshotFlag>)
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'new pre-release creation'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion         | snapshotFlag         |
      | patch         | true      | 0.1.1-RC.1              | non-snapshot         |
      | patch         | false     | 0.1.1-RC.1              | non-snapshot         |
      | patch         | true      | 0.1.1-RC.1-SNAPSHOT     | snapshot             |
      | patch         | false     | 0.1.1-RC.1-SNAPSHOT     | snapshot             |
      | minor         | true      | 0.2.0-RC.1              | non-snapshot         |
      | minor         | false     | 0.2.0-RC.1              | non-snapshot         |
      | minor         | true      | 0.2.0-RC.1-SNAPSHOT     | snapshot             |
      | minor         | false     | 0.2.0-RC.1-SNAPSHOT     | snapshot             |
      | major         | true      | 1.0.0-RC.1              | non-snapshot         |
      | major         | false     | 1.0.0-RC.1              | non-snapshot         |
      | major         | true      | 1.0.0-RC.1-SNAPSHOT     | snapshot             |
      | major         | false     | 1.0.0-RC.1-SNAPSHOT     | snapshot             |

  @new-pre-release
  Scenario Outline: With previous pre-release version will bump pre-release version (Force bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (major,<snapshotFlag>)
    And Following annotated: <annotated> tags (v0.1.0,<previousVersion>) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'bump pre-release version'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | annotated | previousVersion          | expectedVersion     | snapshotFlag         |
      | true      | v0.1.1-RC.1              | 0.1.1-RC.2          | non-snapshot         |
      | false     | v0.1.1-RC.1              | 0.1.1-RC.2          | non-snapshot         |
      | true      | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-RC.2-SNAPSHOT |snapshot              |
      | false     | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-RC.2-SNAPSHOT | snapshot             |
      | true      | v0.2.0-RC.1              | 0.2.0-RC.2          | non-snapshot         |
      | false     | v0.2.0-RC.1              | 0.2.0-RC.2          | non-snapshot         |
      | true      | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-RC.2-SNAPSHOT | snapshot             |
      | false     | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-RC.2-SNAPSHOT | snapshot             |
      | true      | v1.0.0-RC.1              | 1.0.0-RC.2          | non-snapshot         |
      | false     | v1.0.0-RC.1              | 1.0.0-RC.2          | non-snapshot         |
      | true      | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-RC.2-SNAPSHOT | snapshot             |
      | false     | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-RC.2-SNAPSHOT | snapshot             |

  @new-pre-release
  Scenario Outline: With current version is pre-release, promote to release (Force bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (promoteToRelease,<snapshotFlag>)
    And Following annotated: <annotated> tags (v0.1.0,<previousVersion>) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'promote to release'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | annotated | previousVersion          | expectedVersion     | snapshotFlag         |
      | true      | v0.1.1-RC.1              | 0.1.1               | non-snapshot         |
      | false     | v0.1.1-RC.1              | 0.1.1               | non-snapshot         |
      | true      | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-SNAPSHOT      |snapshot              |
      | false     | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-SNAPSHOT      | snapshot             |
      | true      | v0.2.0-RC.1              | 0.2.0               | non-snapshot         |
      | false     | v0.2.0-RC.1              | 0.2.0               | non-snapshot         |
      | true      | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-SNAPSHOT      | snapshot             |
      | false     | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-SNAPSHOT      | snapshot             |
      | true      | v1.0.0-RC.1              | 1.0.0               | non-snapshot         |
      | false     | v1.0.0-RC.1              | 1.0.0               | non-snapshot         |
      | true      | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-SNAPSHOT      | snapshot             |
      | false     | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-SNAPSHOT      | snapshot             |

  @new-pre-release
  Scenario Outline: New pre-release without any previous pre-release version will create new pre-release (Auto bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/auto-bump' at paths (<snapshotFlag>)
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '[new-pre-release] creation with [<bumpComponent>]'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion         | snapshotFlag         |
      | patch         | true      | 0.1.1-RC.1              | non-snapshot         |
      | patch         | false     | 0.1.1-RC.1              | non-snapshot         |
      | patch         | true      | 0.1.1-RC.1-SNAPSHOT     | snapshot             |
      | patch         | false     | 0.1.1-RC.1-SNAPSHOT     | snapshot             |
      | minor         | true      | 0.2.0-RC.1              | non-snapshot         |
      | minor         | false     | 0.2.0-RC.1              | non-snapshot         |
      | minor         | true      | 0.2.0-RC.1-SNAPSHOT     | snapshot             |
      | minor         | false     | 0.2.0-RC.1-SNAPSHOT     | snapshot             |
      | major         | true      | 1.0.0-RC.1              | non-snapshot         |
      | major         | false     | 1.0.0-RC.1              | non-snapshot         |
      | major         | true      | 1.0.0-RC.1-SNAPSHOT     | snapshot             |
      | major         | false     | 1.0.0-RC.1-SNAPSHOT     | snapshot             |

  @new-pre-release
  Scenario Outline: With previous pre-release version will bump pre-release version (Auto bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/auto-bump' at paths (<snapshotFlag>)
    And Following annotated: <annotated> tags (v0.1.0,<previousVersion>) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'bump pre-release version, [major] should be ignored'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | annotated | previousVersion          | expectedVersion      | snapshotFlag         |
      | true      | v0.1.1-RC.1              | 0.1.1-RC.2           | non-snapshot         |
      | false     | v0.1.1-RC.1              | 0.1.1-RC.2           | non-snapshot         |
      | true      | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-RC.2-SNAPSHOT  |snapshot              |
      | false     | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-RC.2-SNAPSHOT  | snapshot             |
      | true      | v0.2.0-RC.1              | 0.2.0-RC.2           | non-snapshot         |
      | false     | v0.2.0-RC.1              |0.2.0-RC.2            | non-snapshot         |
      | true      | v0.2.0-RC.1-SNAPSHOT     |0.2.0-RC.2-SNAPSHOT   | snapshot             |
      | false     | v0.2.0-RC.1-SNAPSHOT     |0.2.0-RC.2-SNAPSHOT   | snapshot             |
      | true      | v1.0.0-RC.1              |1.0.0-RC.2            | non-snapshot         |
      | false     | v1.0.0-RC.1              |1.0.0-RC.2            | non-snapshot         |
      | true      | v1.0.0-RC.1-SNAPSHOT     |1.0.0-RC.2-SNAPSHOT   | snapshot             |
      | false     | v1.0.0-RC.1-SNAPSHOT     |1.0.0-RC.2-SNAPSHOT   | snapshot             |

  @new-pre-release
  Scenario Outline: With current version is pre-release, promote to release (Auto bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/auto-bump' at paths (<snapshotFlag>)
    And Following annotated: <annotated> tags (v0.1.0,<previousVersion>) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '[promote] to release, [minor] should be ignored'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | annotated | previousVersion          | expectedVersion     | snapshotFlag         |
      | true      | v0.1.1-RC.1              | 0.1.1               | non-snapshot         |
      | false     | v0.1.1-RC.1              | 0.1.1               | non-snapshot         |
      | true      | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-SNAPSHOT      | snapshot             |
      | false     | v0.1.1-RC.1-SNAPSHOT     | 0.1.1-SNAPSHOT      | snapshot             |
      | true      | v0.2.0-RC.1              | 0.2.0               | non-snapshot         |
      | false     | v0.2.0-RC.1              | 0.2.0               | non-snapshot         |
      | true      | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-SNAPSHOT      | snapshot             |
      | false     | v0.2.0-RC.1-SNAPSHOT     | 0.2.0-SNAPSHOT      | snapshot             |
      | true      | v1.0.0-RC.1              | 1.0.0               | non-snapshot         |
      | false     | v1.0.0-RC.1              | 1.0.0               | non-snapshot         |
      | true      | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-SNAPSHOT      | snapshot             |
      | false     | v1.0.0-RC.1-SNAPSHOT     | 1.0.0-SNAPSHOT      | snapshot             |
