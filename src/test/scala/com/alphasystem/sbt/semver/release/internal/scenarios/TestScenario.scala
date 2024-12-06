package com.alphasystem
package sbt
package semver
package release
package internal
package scenarios

import release.common.{JGitAdapter, TestRepository}

import scala.util.{Failure, Success, Try}

sealed trait TestScenario {
  protected val commitMessage: String
  protected val config: SemanticBuildVersionConfiguration = SemanticBuildVersionConfiguration()
  val description: String
  val expectedResult: String
  def runScenario(repository: TestRepository, adapter: JGitAdapter): String
}

sealed trait Scenario1 extends TestScenario {
  override val description: String = "create initial commit and create tag using startingVersion"
  override protected val commitMessage: String = "initial commit"
  override val expectedResult: String = "v0.1.0"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .makeChanges()
      .commit(commitMessage)
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    adapter.getCurrentHeadTag
  }
}

sealed trait Scenario2 extends TestScenario {
  override val description: String = "create branch and commit minor version, minor version should be bumped"
  override val expectedResult: String = "v0.2.0"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .createAndCheckout("test")
      .makeChanges()
      .makeChanges()
      .commit(commitMessage)
      .checkoutBranch("main")
      .merge("test")
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    adapter.getCurrentHeadTag
  }
}

sealed trait Scenario3 extends TestScenario {
  override val description: String = "attempt to create tag without commiting anything new"
  override protected val commitMessage: String = ""
  override val expectedResult: String = "Couldn't determine next version, tag (v0.2.0) is already exists."
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String =
    Try(
      repository.createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    ) match {
      case Failure(ex) => ex.getMessage
      case Success(_)  => adapter.getCurrentHeadTag
    }
}

sealed trait Scenario4 extends TestScenario {
  override val description: String = "check out tag and create hot fix tag"
  override val expectedResult: String = "v0.1.0.1"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .checkoutTag("v0.1.0+", "v0.1.0")
      .createAndCheckout("hot_fix")
      .makeChanges()
      .commit(commitMessage)
      .checkoutBranch("v0.1.0+")
      .merge("hot_fix")
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    adapter.getCurrentHeadTag
  }
}

sealed trait Scenario5 extends TestScenario {
  override val description: String = "create new branch from main, merge back to main and generate new tag"
  override protected val commitMessage: String = "updated" // no pattern defined should bump patch version in auto bump
  override val expectedResult: String = "v0.2.1"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .checkoutBranch("main")
      .createAndCheckout("update_tag")
      .makeChanges()
      .commit(commitMessage)
      .checkoutBranch("main")
      .merge("update_tag")
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    adapter.getCurrentHeadTag
  }
}

sealed trait Scenario6 extends TestScenario {
  override val description: String = "create new tag in hot fix branch v0.1.0+"
  override val expectedResult: String = "v0.1.0.2"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .checkoutBranch("v0.1.0+")
      .createAndCheckout("update_hotfix_tag")
      .makeChanges()
      .commit(commitMessage)
      .checkoutBranch("v0.1.0+")
      .merge("update_hotfix_tag")
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    adapter.getCurrentHeadTag
  }
}

sealed trait Scenario7 extends TestScenario {
  override val description: String = "bump major version"
  override val expectedResult: String = "v1.0.0"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .checkoutBranch("main")
      .createAndCheckout("bump_major")
      .makeChanges()
      .commit(commitMessage)
      .checkoutBranch("main")
      .merge("bump_major")
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    adapter.getCurrentHeadTag
  }
}

sealed trait Scenario8 extends TestScenario {
  override val description: String = "create new pre-release with minor bump"
  override val expectedResult: String = "v1.1.0-RC.1"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .checkoutBranch("main")
      .createAndCheckout("new_pre_release")
      .makeChanges()
      .commit(commitMessage)
      .checkoutBranch("main")
      .merge("new_pre_release")
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    adapter.getCurrentHeadTag
  }
}

sealed trait Scenario9 extends TestScenario {
  override val description: String = "bump new release version"
  override val expectedResult: String = "v1.1.0-RC.2"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .checkoutBranch("main")
      .createAndCheckout("new_pre_release_2")
      .makeChanges()
      .commit(commitMessage)
      .checkoutBranch("main")
      .merge("new_pre_release_2")
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    adapter.getCurrentHeadTag
  }
}

sealed trait Scenario10 extends TestScenario {
  override val description: String = "promote to release"
  override val expectedResult: String = "v1.1.0"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .checkoutBranch("main")
      .createAndCheckout("promote_to_release")
      .makeChanges()
      .commit(commitMessage)
      .checkoutBranch("main")
      .merge("promote_to_release")
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    adapter.getCurrentHeadTag
  }
}

sealed trait Scenario11 extends TestScenario {
  override val description: String = "ignore promote to release since it is not a pre-release version"
  override val expectedResult: String = "v1.1.1"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .checkoutBranch("main")
      .createAndCheckout("ignore_promote_to_release")
      .makeChanges()
      .commit(commitMessage)
      .checkoutBranch("main")
      .merge("ignore_promote_to_release")
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    adapter.getCurrentHeadTag
  }
}

sealed trait Scenario12 extends TestScenario {
  override val description: String = "create snapshot version when create tag from branch"
  override lazy val expectedResult: String = s"v1.1.2-SNAPSHOT"
  override def runScenario(repository: TestRepository, adapter: JGitAdapter): String = {
    repository
      .checkoutBranch("main")
      .createAndCheckout("sub_branch")
      .makeChanges()
      .commit(commitMessage)
      .createTag(SemanticBuildVersion(repository.workingDirectory, config).determineVersion)
    val result = adapter.getCurrentHeadTag
    val index = result.indexOf("+")
    result.substring(0, index)
  }
}

object AutoBumpScenarios {

  val scenarios: Seq[TestScenario] =
    Seq(
      new Scenario1 {},
      new Scenario2 {
        override protected val commitMessage: String = "[minor] version update"
      },
      new Scenario3 {},
      new Scenario4 {
        override protected val commitMessage: String = "some commit [minor]" // minor bump will be ignored since we are in hot fix branch
      },
      new Scenario5 {},
      new Scenario6 {
        override protected val commitMessage: String = "updated [major]" // major pattern would be ignored
      },
      new Scenario7 {
        override protected val commitMessage: String = "bump [major] version"
      },
      new Scenario8 {
        override protected val commitMessage: String = "creating [new-pre-release] with [minor] bump"
      },
      new Scenario9 {
        override protected val commitMessage: String = "bump pre-release version [patch] bump will be ignored"
      },
      new Scenario10 {
        override protected val commitMessage: String = "[promote] to release [major] bump will be ignored"
      },
      new Scenario11 {
        override protected val commitMessage: String = s"[promote] to release will be ignored"
      },
      new Scenario12 {
        override protected val commitMessage: String = "commiting from branch [patch]"
      }
    )
}

object ForceBumpScenarios {

  sealed trait ForceBumpScenario extends TestScenario {
    protected val baseConfig: SemanticBuildVersionConfiguration = SemanticBuildVersionConfiguration(forceBump = true)
    override protected val config: SemanticBuildVersionConfiguration = baseConfig
  }

  val scenarios: Seq[TestScenario] =
    Seq(
      new Scenario1 with ForceBumpScenario {},
      new Scenario2 with ForceBumpScenario {
        override protected val commitMessage: String = "version update"
        override protected val config: SemanticBuildVersionConfiguration =
          baseConfig.copy(componentToBump = VersionComponent.MINOR)
      },
      new Scenario3 with ForceBumpScenario {},
      new Scenario4 with ForceBumpScenario {
        override protected val commitMessage: String = "some commit"
        override protected val config: SemanticBuildVersionConfiguration =
          baseConfig.copy(componentToBump = VersionComponent.MINOR) // minor bump will be ignored since we are in hot fix branch
      },
      new Scenario5 with ForceBumpScenario {
        override protected val config: SemanticBuildVersionConfiguration =
          baseConfig.copy(componentToBump = VersionComponent.PATCH)
      },
      new Scenario6 with ForceBumpScenario {
        override protected val commitMessage: String = "hot fix branch but updating major"
        override protected val config: SemanticBuildVersionConfiguration =
          baseConfig.copy(componentToBump = VersionComponent.MAJOR)
      },
      new Scenario7 with ForceBumpScenario {
        override protected val commitMessage: String = "force bump major version"
        override protected val config: SemanticBuildVersionConfiguration =
          baseConfig.copy(componentToBump = VersionComponent.MAJOR)
      },
      new Scenario8 with ForceBumpScenario {
        override protected val commitMessage: String = "force creating new-pre-release with minor bump"
        override protected val config: SemanticBuildVersionConfiguration =
          baseConfig.copy(componentToBump = VersionComponent.MINOR, newPreRelease = true)
      },
      new Scenario9 with ForceBumpScenario {
        override protected val commitMessage: String = "force bump pre-release version patch bump will be ignored"
        override protected val config: SemanticBuildVersionConfiguration =
          baseConfig.copy(componentToBump = VersionComponent.PATCH)
      },
      new Scenario10 with ForceBumpScenario {
        override protected val commitMessage: String = "force promote to release major bump will be ignored"
        override protected val config: SemanticBuildVersionConfiguration =
          baseConfig.copy(componentToBump = VersionComponent.MAJOR, promoteToRelease = true)
      },
      new Scenario11 with ForceBumpScenario {
        override protected val commitMessage: String = "promote to release will be ignored"
        override protected val config: SemanticBuildVersionConfiguration =
          baseConfig.copy(componentToBump = VersionComponent.PATCH, promoteToRelease = true)
      },
      new Scenario12 with ForceBumpScenario {
        override protected val commitMessage: String = "commiting from branch"
        override protected val config: SemanticBuildVersionConfiguration =
          baseConfig.copy(componentToBump = VersionComponent.PATCH)
      }
    )
}
