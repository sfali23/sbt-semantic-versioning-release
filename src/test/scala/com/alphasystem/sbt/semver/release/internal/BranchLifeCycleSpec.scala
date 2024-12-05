package com.alphasystem
package sbt
package semver
package release
package internal

import com.alphasystem.sbt.semver.release.common.{JGitAdapter, TestRepository}
import com.alphasystem.sbt.semver.release.internal.scenarios.TestScenario
import org.eclipse.jgit.util.FileUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.nio.file.Paths

abstract class BranchLifeCycleSpec(workingDirectory: File, testDescription: String, scenarios: Seq[TestScenario])
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfterAll {

  protected val repository: TestRepository = TestRepository(workingDirectory)
  protected val adapter: JGitAdapter = JGitAdapter(workingDirectory)

  override protected def afterAll(): Unit = {
    super.afterAll()
    FileUtils.delete(workingDirectory, FileUtils.RECURSIVE)
  }

  s"$testDescription" should {
    scenarios.foreach { scenario =>
      s"${scenario.description}" in {
        val actualVersion = scenario.runScenario(repository, adapter)
        actualVersion shouldBe scenario.expectedResult
      }
    }
  }
}

class AutoBumpBranchLifeCycleSpec
    extends BranchLifeCycleSpec(
      Paths.get("auto-bump-branch").toFile,
      "Branch with autoBump configuration",
      scenarios.AutoBumpScenarios.scenarios
    )

class ForceBumpBranchLifeCycleSpec
    extends BranchLifeCycleSpec(
      Paths.get("force-bump-branch").toFile,
      "Branch with forceBump configuration",
      scenarios.AutoBumpScenarios.scenarios
    )
