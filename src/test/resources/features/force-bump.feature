Feature: Branch with Force bump repository

  @force-bump
  Scenario Outline: Create initial commit and create tag using startingVersion
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths ()
    When Make changes and commit with message: 'initial commit'
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '0.1.0'
    And Close resources

    Examples:
    | annotated |
    | true      |
    | false     |

  @force-bump
  Scenario Outline: Create branch and commit minor version, minor version should be bumped
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (minor)
    And Following annotated: <annotated> tags (v0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'version update'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '0.2.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Attempt to create tag without commiting anything new
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths ()
    And Following annotated: <annotated> tags (v0.1.0) has been created
    When No changes made to repository
    Then Exception 'Couldn't determine next version, tag (0.1.0) is already exists.' should be thrown when creating new tag
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Check out tag and create hot fix tag
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (major)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0) has been created
    When A Tag 'v0.1.0' has been checked out
    And Branch 'hot_fix' is created and checked out
    And Make changes and commit with message: 'hot fix version will created and major version be ignored'
    And Branch 'v0.1.0+' is checked out
    And Merge branch 'hot_fix' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '0.1.0.1'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Create new branch from main, merge back to main and generate new tag
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (patch)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'updated'
    And Branch 'main' is checked out
    And Merge branch 'updated_tag' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '0.2.1'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Create new tag in hot fix branch v0.1.0+
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (minor)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0) has been created
    When A Tag 'v0.1.0' has been checked out
    And Branch 'hot_fix' is created and checked out
    And Make changes and commit with message: 'hot fix version will created and major version be ignored'
    And Branch 'v0.1.0+' is checked out
    And Merge branch 'hot_fix' into current branch
    And A tag with annotated: (<annotated>) flag is created
    And Branch 'hot_fix_2' is created and checked out
    And Make changes and commit with message: 'hot fix version will created and major version be ignored'
    And Branch 'v0.1.0+' is checked out
    And Merge branch 'hot_fix_2' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '0.1.0.2'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Bump major version
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (major)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'updated'
    And Branch 'main' is checked out
    And Merge branch 'updated_tag' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Create new pre-release with minor bump
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (minor,newPreRelease)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'updated'
    And Branch 'main' is checked out
    And Merge branch 'updated_tag' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.1.0-RC.1'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Bump pre release version
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (minor)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0,v1.1.0-RC.1) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'updated'
    And Branch 'main' is checked out
    And Merge branch 'updated_tag' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.1.0-RC.2'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Promote to release version
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (major,promoteToRelease)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0,v1.1.0-RC.1,v1.1.0-RC.2) has been created
    When Branch 'promote_pre_release' is created and checked out
    And Make changes and commit with message: 'updated'
    And Branch 'main' is checked out
    And Merge branch 'promote_pre_release' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.1.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Ignore promote to release since it is not a pre-release version
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (patch,promoteToRelease)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'ignore_promote_to_release' is created and checked out
    And Make changes and commit with message: 'updated'
    And Branch 'main' is checked out
    And Merge branch 'ignore_promote_to_release' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.1'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Create new pre-release without specifying bump version will fail the build
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (newPreRelease)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'updated_tag' is created and checked out
    And Make changes and commit with message: 'updated'
    And Branch 'main' is checked out
    And Merge branch 'updated_tag' into current branch
    Then Exception 'Couldn't determine next version, tag (1.0.0) is already exists.' should be thrown when creating new tag
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Create snapshot version when create tag from branch
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (minor)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'snapshot_branch' is created and checked out
    And Make changes and commit with message: 'updated'
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.1.0-SNAPSHOT'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Create snapshot version when snapshot flag is set to true
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (patch,snapshot)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Branch 'new_branch' is created and checked out
    And Make changes and commit with message: 'updated'
    And Branch 'main' is checked out
    And Merge branch 'new_branch' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.1-SNAPSHOT'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Create snapshot version when branch has uncommitted changes
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (patch,snapshot)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Make some changes
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.1-SNAPSHOT'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Create snapshot version when branch has uncommitted changes
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (patch)
    And Following annotated: <annotated> tags (v0.1.0,v0.2.0,v1.0.0) has been created
    When Make some changes
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.1-SNAPSHOT'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Override tagPrefix will generate tags with given prefix
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (major,tagPrefix)
    And Following annotated: <annotated> tags (alpha.0.1.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'version update'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

  @force-bump
  Scenario Outline: Ignore unmatched tags with prefix other than configured prefix
    Given Current branch is 'main'
    And Read build config from resource 'configs/force-bump' at paths (major,tagPrefix)
    And Following annotated: <annotated> tags (v.0.1.0,alpha.0.1.0,v1.0.0) has been created
    And Branch 'test' is created and checked out
    When Make changes and commit with message: 'version update'
    And Branch 'main' is checked out
    And Merge branch 'test' into current branch
    And A tag with annotated: (<annotated>) flag is created
    Then Generated version should be '1.0.0'
    And Close resources

    Examples:
      | annotated |
      | true      |
      | false     |

