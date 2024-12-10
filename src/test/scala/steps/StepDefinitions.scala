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
import scala.util.Try

class StepDefinitions extends ScalaDsl with EN with Matchers {

  private val workingDirectory: File = Files.createTempDirectory(UUID.randomUUID().toString).toFile
  private val repository: TestRepository = TestRepository(workingDirectory)
  private val adapter: JGitAdapter = JGitAdapter(workingDirectory)
  private var config: SemanticBuildVersionConfiguration = SemanticBuildVersionConfiguration()

  ParameterType("bool", ".*") { (value: String) =>
    Option(value).exists(_.toBoolean)
  }

  Given("""Read build config from resource {string} at paths \({})""") { (resourceName: String, paths: String) =>
    config = toSemanticBuildVersionConfiguration(resourceName, paths.split(",").filterNot(_.isBlank))
  }

  Given("""Load snapshot config \({})""") { (conf: String) =>
    config = config.copy(snapshotConfig = toSnapshotConfig(conf))
  }

  Given("""Current branch is {string}""") { (branchName: String) =>
    if (repository.getBranchName != branchName) repository.checkoutBranch(branchName)
  }

  Given("""Following annotated: {bool} tags \({}) has been created""") { (annotated: Boolean, tags: String) =>
    tags.split(",").foreach(tag => repository.commitAndTag(tag, annotated))
  }

  When("""Branch {string} is created and checked out""") { (branchName: String) =>
    repository.createAndCheckout(branchName)
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
      snapshotSuffix = snapshotConfig.suffix,
      preReleaseConfig = config.preReleaseConfig
    )
    val result =
      if (currentTag.contains(snapshotConfig.suffix)) {
        if (snapshotConfig.appendCommitHash) {
          val hash =
            if (snapshotConfig.useShortHash) adapter.getShortHash
            else adapter.getHeadCommit.getName

          s"$expectedVersion+$hash"
        } else expectedVersion
      } else expectedVersion

    println(result)
    currentTag shouldBe result
  }

  Then("Close resources") { () =>
    Try(repository.close())
    Try(FileUtils.delete(workingDirectory, FileUtils.RECURSIVE))
  }

}
