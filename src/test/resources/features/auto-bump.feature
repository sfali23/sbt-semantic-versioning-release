Feature: Branch with Auto bump repository

  @auto-bump
  Scenario Outline: Create initial commit and create tag using startingVersion
    Given Current branch is 'main'
    When Make changes and commit with message: 'initial commit'
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '0.1.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Create branch and commit minor version, minor version should be bumped
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '[minor] version update'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '0.2.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Attempt to create tag without commiting anything new
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0) has been created
    When No changes made to repository
    Then Exception 'Couldn't determine next version, tag (0.1.0) is already exists.' should be thrown when creating new tag
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Check out tag and create hot fix tag
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0) has been created
    When A Tag 'v0.1.0' has been checked out
    And Branch 'hot_fix' is created and checked out
    And Make changes and commit with message: 'hot fix version will created and [major] version be ignored'
    And Branch 'v0.1.0+' is checked out
    And Merge branch 'hot_fix' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '0.1.0.1'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Create new branch from main, merge back to main and generate new tag
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'no pattern is defined should bump patch version'
    And Branch 'main' is checked out
    And Merge branch 'updated_tag' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '0.2.1'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Create new tag in hot fix branch v0.1.0+
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0) has been created
    When A Tag 'v0.1.0' has been checked out
    And Branch 'hot_fix' is created and checked out
    And Make changes and commit with message: 'hot fix version will created and [major] version be ignored'
    And Branch 'v0.1.0+' is checked out
    And Merge branch 'hot_fix' into current branch
    And A tag with annotated: (<annotated>) flag is created
    And Branch 'hot_fix_2' is created and checked out
    And Make changes and commit with message: 'hot fix version will created and [minor] version be ignored'
    And Branch 'v0.1.0+' is checked out
    And Merge branch 'hot_fix_2' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '0.1.0.2'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Bump major version
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: '[major] version bump'
    And Branch 'main' is checked out
    And Merge branch 'updated_tag' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Create new pre-release with minor bump
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'creating [new-pre-release] with [minor] bump'
    And Branch 'main' is checked out
    And Merge branch 'updated_tag' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.1.0-RC.1'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Bump pre release version
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0,v1.1.0-RC.1) has been created
    When Branch 'new_pre_release_2' is created and checked out
    And Make changes and commit with message: 'bumping pre-release version with [minor] bump will be ignored'
    And Branch 'main' is checked out
    And Merge branch 'new_pre_release_2' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.1.0-RC.2'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Promote to release version
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0,v1.1.0-RC.1,v1.1.0-RC.2) has been created
    When Branch 'promote_pre_release' is created and checked out
    And Make changes and commit with message: '[promote] pre-release version with [major] bump will be ignored'
    And Branch 'main' is checked out
    And Merge branch 'promote_pre_release' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.1.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Ignore promote to release since it is not a pre-release version
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'ignore_promote_to_release' is created and checked out
    And Make changes and commit with message: '[promote] pre-release version'
    And Branch 'main' is checked out
    And Merge branch 'ignore_promote_to_release' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.1'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Create new pre-release without specifying bump version will bump patch version
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'creating [new-pre-release]'
    And Branch 'main' is checked out
    And Merge branch 'updated_tag' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.1-RC.1'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Create snapshot version when create tag from branch
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'snapshot_branch' is created and checked out
    And Make changes and commit with message: 'creating [minor]'
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.1.0-SNAPSHOT'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Create snapshot version when branch has uncommitted changes
    Given Current branch is 'main'
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    And Make some changes
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.1-SNAPSHOT'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Override tagPrefix will generate tags with given prefix
    Given Current branch is 'main'
    And Read build config from resource 'configs/auto-bump' at paths (tagPrefix)
    And Following annotated: <annotated> tags (alpha.0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '[major] version update'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Ignore unmatched tags with prefix other than configured prefix
    Given Current branch is 'main'
    And Read build config from resource 'configs/auto-bump' at paths (tagPrefix)
    And Following annotated: <annotated> tags (v.0.1.0,alpha.0.1.0,v1.0.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '[major] version update'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |
