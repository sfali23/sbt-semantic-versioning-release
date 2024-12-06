package steps

import com.alphasystem.sbt.semver.release.common.{JGitAdapter, TestRepository}
import com.alphasystem.sbt.semver.release.internal.{SemanticBuildVersion, SemanticBuildVersionConfiguration}
import com.alphasystem.sbt.semver.release.test.*
import io.cucumber.scala.{EN, ScalaDsl}
import org.eclipse.jgit.util.FileUtils

import java.io.File
import java.nio.file.Files

class StepDefinitions extends ScalaDsl with EN {

  import StepDefinitions.*

  private var config = SemanticBuildVersionConfiguration()

  ParameterType("bool", ".*") { (value: String) =>
    Option(value).exists(_.toBoolean)
  }

  Given("""Read build config from resource {string}""") { (resourceName: String) =>
    config = toSemanticBuildVersionConfiguration(resourceName)
  }

  Given("""Current branch is {string}""") { (branchName: String) =>
    if (repository.getBranchName != branchName) repository.checkoutBranch(branchName)
  }

  When("""Branch {string} is created and checked out""") { (branchName: String) =>
    repository.createAndCheckout(branchName)
  }

  When("""Branch is checked out {string}""") { (branchName: String) =>
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

  When("""Create an annotated tag: {bool}""") { (annotated: Boolean) =>
    repository.tag(SemanticBuildVersion(workingDirectory, config).toStringValue, annotated)
  }

  Then("""Generated version should be {string}""") { (expectedVersion: String) =>
    assert(
      adapter.getCurrentHeadTag(
        tagPrefix = config.tagPrefix,
        snapshotSuffix = config.snapshotSuffix,
        preReleaseConfig = config.preReleaseConfig
      ) == expectedVersion
    )
  }

}

object StepDefinitions extends ScalaDsl {
  private val workingDirectory: File = Files.createTempDirectory("cucumber-test-").toFile
  private val repository: TestRepository = TestRepository(workingDirectory)
  private val adapter: JGitAdapter = JGitAdapter(workingDirectory)

  BeforeAll {
    println(s"Working directory: ${workingDirectory.toPath}")
  }

  AfterAll {
    repository.close()
    FileUtils.delete(workingDirectory, FileUtils.RECURSIVE)
  }
}
