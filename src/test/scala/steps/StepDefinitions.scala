package steps

import com.alphasystem.sbt.semver.release.common.{JGitAdapter, TestRepository}
import com.alphasystem.sbt.semver.release.internal.{SemanticBuildVersion, SemanticBuildVersionConfiguration}
import com.alphasystem.sbt.semver.release.test.*
import io.cucumber.scala.{EN, ScalaDsl}
import org.eclipse.jgit.util.FileUtils
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files
import java.util.UUID
import scala.util.{Failure, Success, Try}

class StepDefinitions extends ScalaDsl with EN with Matchers {

  private val workingDirectory: File = Files.createTempDirectory(UUID.randomUUID().toString).toFile
  private val repository: TestRepository = TestRepository(workingDirectory)
  private val adapter: JGitAdapter = JGitAdapter(workingDirectory)
  private var config: SemanticBuildVersionConfiguration = SemanticBuildVersionConfiguration()
  private var mainBranchName = ""

  ParameterType("bool", ".*") { (value: String) =>
    Option(value).exists(_.toBoolean)
  }

  Given("""Load semantic build config from \({})""") { (conf: String) =>
    config = toSemanticBuildVersionConfiguration(conf)
  }

  Given("Record main branch") { () =>
    mainBranchName = repository.getBranchName
  }

  Given("""Following annotated: {bool} tags \({}) has been created""") { (annotated: Boolean, tags: String) =>
    tags.split(",").foreach(tag => repository.commitAndTag(tag, annotated))
  }

  When("""Branch {string} is created and checked out""") { (branchName: String) =>
    repository.createAndCheckout(branchName)
  }

  When("Main branch is checked out") { () =>
    repository.checkoutBranch(mainBranchName)
  }

  When("""Branch {string} is checked out""") { (branchName: String) =>
    repository.checkoutBranch(branchName)
  }

  When("""Merge branch {string} into current branch""") { (branchName: String) =>
    repository.merge(branchName)
  }

  When("""Make some changes""") { () =>
    repository.makeChanges()
  }

  When("""Commit with message: {string}""") { (commitMessage: String) =>
    repository.commit(commitMessage)
  }

  When("Make changes and commit with message: {string}") { (commitMessage: String) =>
    repository.makeChanges().commit(commitMessage)
  }

  When("A Tag {string} has been checked out") { (tag: String) =>
    repository.checkoutTag(s"$tag+", tag)
  }

  When("""A tag with annotated: \({bool}) flag is created""") { (annotated: Boolean) =>
    repository.tag(SemanticBuildVersion(workingDirectory, config).determineVersion, annotated)
  }

  When("No changes made to repository") { () => }

  Then("""Exception '{}' should be thrown when creating new tag""") { (message: String) =>
    val ex =
      intercept[IllegalArgumentException](
        SemanticBuildVersion(workingDirectory, config).determineVersion
      )
    ex.getMessage shouldBe message
  }

  Then("""Generated version should be {string}""") { (expectedVersion: String) =>
    val snapshotConfig = config.snapshotConfig
    val currentTag = adapter.getCurrentHeadTag(
      tagPrefix = config.tagPrefix,
      snapshotSuffix = snapshotConfig.prefix,
      preReleaseConfig = config.preReleaseConfig
    )
    val result =
      if (currentTag.contains(snapshotConfig.prefix)) {
        if (snapshotConfig.appendCommitHash) {
          val hash =
            if (snapshotConfig.useShortHash) adapter.getShortHash
            else adapter.getHeadCommit.getName

          s"$expectedVersion+$hash"
        } else expectedVersion
      } else expectedVersion

    currentTag shouldBe result
  }

  Then("Close resources") { () =>
    Try(repository.close())
    Try(FileUtils.delete(workingDirectory, FileUtils.RECURSIVE))
  }

}
