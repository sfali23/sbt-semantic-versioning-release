Feature: Snapshot

  @snapshot
  Scenario Outline: Create snapshot release with overridden suffix (Force bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (<bumpComponent>,snapshot)
    And Load snapshot config ({"suffix": "snapshot"})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion   |
      | patch         | true      | v0.1.1-snapshot   |
      | patch         | false     | v0.1.1-snapshot   |
      | minor         | true      | v0.2.0-snapshot   |
      | minor         | false     | v0.2.0-snapshot   |
      | major         | true      | v1.0.0-snapshot   |
      | major         | false     | v1.0.0-snapshot   |

  @snapshot
  Scenario Outline: Create snapshot release with overridden suffix and using long hash commit (Force bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (<bumpComponent>,snapshot)
    And Load snapshot config ({"suffix": "snapshot", "useShortHash": false})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion   |
      | patch         | true      | v0.1.1-snapshot   |
      | patch         | false     | v0.1.1-snapshot   |
      | minor         | true      | v0.2.0-snapshot   |
      | minor         | false     | v0.2.0-snapshot   |
      | major         | true      | v1.0.0-snapshot   |
      | major         | false     | v1.0.0-snapshot   |

  @snapshot
  Scenario Outline: Create snapshot release with overridden suffix and no hash commit (Force bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (<bumpComponent>,snapshot)
    And Load snapshot config ({"suffix": "snapshot", "appendCommitHash": false})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion   |
      | patch         | true      | v0.1.1-snapshot   |
      | patch         | false     | v0.1.1-snapshot   |
      | minor         | true      | v0.2.0-snapshot   |
      | minor         | false     | v0.2.0-snapshot   |
      | major         | true      | v1.0.0-snapshot   |
      | major         | false     | v1.0.0-snapshot   |

  @snapshot
  Scenario Outline: Create snapshot release with overridden suffix (Auto bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/auto-bump' at paths (snapshot)
    And Load snapshot config ({"suffix": "snapshot"})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot creation with [<bumpComponent>]'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion   |
      | patch         | true      | v0.1.1-snapshot   |
      | patch         | false     | v0.1.1-snapshot   |
      | minor         | true      | v0.2.0-snapshot   |
      | minor         | false     | v0.2.0-snapshot   |
      | major         | true      | v1.0.0-snapshot   |
      | major         | false     | v1.0.0-snapshot   |

  @snapshot
  Scenario Outline: Create snapshot release with overridden suffix and using long hash commit (Auto bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/auto-bump' at paths (snapshot)
    And Load snapshot config ({"suffix": "snapshot", "useShortHash": false})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot with [<bumpComponent>]'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion   |
      | patch         | true      | v0.1.1-snapshot   |
      | patch         | false     | v0.1.1-snapshot   |
      | minor         | true      | v0.2.0-snapshot   |
      | minor         | false     | v0.2.0-snapshot   |
      | major         | true      | v1.0.0-snapshot   |
      | major         | false     | v1.0.0-snapshot   |

  @snapshot
  Scenario Outline: Create snapshot release with overridden suffix and no hash commit (Auto bump)
    Given Current branch is 'main'
    And Read build config from resource 'configs/auto-bump' at paths (snapshot)
    And Load snapshot config ({"suffix": "snapshot", "appendCommitHash": false})
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'snapshot with [<bumpComponent>]'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | bumpComponent | annotated | expectedVersion   |
      | patch         | true      | v0.1.1-snapshot   |
      | patch         | false     | v0.1.1-snapshot   |
      | minor         | true      | v0.2.0-snapshot   |
      | minor         | false     | v0.2.0-snapshot   |
      | major         | true      | v1.0.0-snapshot   |
      | major         | false     | v1.0.0-snapshot   |
