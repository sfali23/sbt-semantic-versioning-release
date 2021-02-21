package com.alphasystem.sbt.semver.release

import com.alphasystem.sbt.semver.release.test._
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class PreReleaseAutobumpingSpec
    extends AnyFunSuite
    with TableDrivenPropertyChecks
    with Matchers {

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(s"promoting pre-release version (annotated: $annotated)") {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commitAndTag("0.2.1-alpha.1", annotated)
            .makeChanges()
            .commitAndTag("0.2.1-beta.1", annotated)
            .makeChanges()
            .commit("This is a message [promote]")

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            SemanticBuildVersionConfiguration(
              tagPattern = "beta".r,
              tagPrefix = "beta.",
              snapshot = false,
              promoteToRelease = true
            )
          ).determineVersion shouldBe "0.2.1"
      }
    }
  }
}
