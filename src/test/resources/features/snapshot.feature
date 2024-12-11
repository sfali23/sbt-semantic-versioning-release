Feature: Snapshot

  @snapshot
  Scenario Outline: Create snapshot release with overridden prefix (Force bump)
    Given Record main branch
    And Load semantic build config from ({forceBump=true, componentToBump=<bumpComponent>, snapshot=true, snapshotConfig={prefix=snapshot}})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot'
    And Main branch is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion  |
      | PATCH         | true      | 0.1.1-snapshot   |
      | PATCH         | false     | 0.1.1-snapshot   |
      | MINOR         | true      | 0.2.0-snapshot   |
      | MINOR         | false     | 0.2.0-snapshot   |
      | MAJOR         | true      | 1.0.0-snapshot   |
      | MAJOR         | false     | 1.0.0-snapshot   |

  @snapshot
  Scenario Outline: Create snapshot release with overridden prefix and using long hash commit (Force bump)
    Given Record main branch
    And Load semantic build config from ({forceBump=true, componentToBump=<bumpComponent>, snapshot=true, snapshotConfig={prefix=snapshot, useShortHash=false}})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot'
    And Main branch is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion  |
      | PATCH         | true      | 0.1.1-snapshot   |
      | PATCH         | false     | 0.1.1-snapshot   |
      | MINOR         | true      | 0.2.0-snapshot   |
      | MINOR         | false     | 0.2.0-snapshot   |
      | MAJOR         | true      | 1.0.0-snapshot   |
      | MAJOR         | false     | 1.0.0-snapshot   |

  @snapshot
  Scenario Outline: Create snapshot release with overridden suffix and no hash commit (Force bump)
    Given Record main branch
    And Load semantic build config from ({forceBump=true, componentToBump=<bumpComponent>, snapshot=true, snapshotConfig={prefix=snapshot, appendCommitHash=false}})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot'
    And Main branch is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion  |
      | PATCH         | true      | 0.1.1-snapshot   |
      | PATCH         | false     | 0.1.1-snapshot   |
      | MINOR         | true      | 0.2.0-snapshot   |
      | MINOR         | false     | 0.2.0-snapshot   |
      | MAJOR         | true      | 1.0.0-snapshot   |
      | MAJOR         | false     | 1.0.0-snapshot   |

  @snapshot
  Scenario Outline: Create snapshot release with overridden prefix (Auto bump)
    Given Record main branch
    And Load semantic build config from ({snapshot=true, snapshotConfig={prefix=snapshot}})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot creation with [<bumpComponent>]'
    And Main branch is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion  |
      | patch         | true      | 0.1.1-snapshot   |
      | patch         | false     | 0.1.1-snapshot   |
      | minor         | true      | 0.2.0-snapshot   |
      | minor         | false     | 0.2.0-snapshot   |
      | major         | true      | 1.0.0-snapshot   |
      | major         | false     | 1.0.0-snapshot   |

  @snapshot
  Scenario Outline: Create snapshot release with overridden prefix and using long hash commit (Auto bump)
    Given Record main branch
    And Load semantic build config from ({snapshot=true, snapshotConfig={prefix=snapshot, useShortHash=false}})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot with [<bumpComponent>]'
    And Main branch is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion   |
      | patch         | true      | 0.1.1-snapshot   |
      | patch         | false     | 0.1.1-snapshot   |
      | minor         | true      | 0.2.0-snapshot   |
      | minor         | false     | 0.2.0-snapshot   |
      | major         | true      | 1.0.0-snapshot   |
      | major         | false     | 1.0.0-snapshot   |

  @snapshot
  Scenario Outline: Create snapshot release with overridden suffix and no hash commit (Auto bump)
    Given Record main branch
    And Load semantic build config from ({snapshot=true, snapshotConfig={prefix=snapshot, appendCommitHash=false}})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot with [<bumpComponent>]'
    And Main branch is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion   |
      | patch         | true      | 0.1.1-snapshot   |
      | patch         | false     | 0.1.1-snapshot   |
      | minor         | true      | 0.2.0-snapshot   |
      | minor         | false     | 0.2.0-snapshot   |
      | major         | true      | 1.0.0-snapshot   |
      | major         | false     | 1.0.0-snapshot   |
