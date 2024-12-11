Feature: Branch with Auto bump repository

  @auto-bump
  Scenario Outline: Create initial commit and create tag using startingVersion
    Given Record main branch
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
    Given Record main branch
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '[minor] version update'
    And Main branch is checked out
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
    Given Record main branch
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
    Given Record main branch
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
    Given Record main branch
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'no pattern is defined should bump patch version'
    And Main branch is checked out
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
    Given Record main branch
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
    Given Record main branch
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: '[major] version bump'
    And Main branch is checked out
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
    Given Record main branch
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'creating [new-pre-release] with [minor] bump'
    And Main branch is checked out
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
    Given Record main branch
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0,v1.1.0-RC.1) has been created
    When Branch 'new_pre_release_2' is created and checked out
    And Make changes and commit with message: 'bumping pre-release version with [minor] bump will be ignored'
    And Main branch is checked out
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
    Given Record main branch
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0,v1.1.0-RC.1,v1.1.0-RC.2) has been created
    When Branch 'promote_pre_release' is created and checked out
    And Make changes and commit with message: '[promote] pre-release version with [major] bump will be ignored'
    And Main branch is checked out
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
    Given Record main branch
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'ignore_promote_to_release' is created and checked out
    And Make changes and commit with message: '[promote] pre-release version'
    And Main branch is checked out
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
    Given Record main branch
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'creating [new-pre-release]'
    And Main branch is checked out
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
    Given Record main branch
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
    Given Record main branch
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
    Given Record main branch
    And Load semantic build config from ({tagPrefix=alpha.})
    And Following annotated: <annotated> tags (alpha.0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '[major] version update'
    And Main branch is checked out
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
    Given Record main branch
    And Load semantic build config from ({tagPrefix=alpha.})
    And Following annotated: <annotated> tags (v.0.1.0,alpha.0.1.0,v1.0.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '[major] version update'
    And Main branch is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @auto-bump
  Scenario Outline: Non semantic version tags should be ignored
    Given Record main branch
    And Following annotated: <annotated> tags (v.0.1.0,v1.0,alpha.1.0.0,v0.5.1) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'version update for [<componentToBump>]'
    And Main branch is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | componentToBump  | annotated | expectedVersion |
      | patch            | true      | 0.5.2           |
      | patch            | false     | 0.5.2           |
      | minor            | true      | 0.6.0           |
      | minor            | false     | 0.6.0           |
      | major            | true      | 1.0.0           |
      | major            | false     | 1.0.0           |

  @auto-bump @new
  Scenario Outline: Custom commit messages
    Given Record main branch
    And Load semantic build config from ({autoBump={majorPattern="^BREAKING[- ]CHANGE:", minorPattern="^feat:", patchPattern="^fix:"}})
    And Following annotated: <annotated> tags (v0.5.1) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: '<componentToBump> custom commit pattern'
    And Main branch is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '<expectedVersion>'
    And Close resources

    Examples:
      | componentToBump   | annotated | expectedVersion |
      | fix:              | true      | 0.5.2           |
      | fix:              | false     | 0.5.2           |
      | feat:             | true      | 0.6.0           |
      | feat:             | false     | 0.6.0           |
      | BREAKING CHANGE:  | true      | 1.0.0           |
      | BREAKING CHANGE:  | false     | 1.0.0           |
