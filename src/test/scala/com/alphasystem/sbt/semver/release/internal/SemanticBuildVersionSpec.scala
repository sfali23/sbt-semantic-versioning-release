package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.{VersionComponent, internal}
import com.alphasystem.sbt.semver.release.test.*
import org.scalatest.Assertion
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

// Working
class SemanticBuildVersionSpec extends AnyFunSpec with TableDrivenPropertyChecks with Matchers {

  describe("test various latest version variants") {
    val data =
      Table(
        ("testName", "tags", "annotated", "expectedVersion"),
        (
          "correct",
          List(
            "0.0.1",
            "0.0.2",
            "0.1.0",
            "0.1.1",
            "1.0.0",
            "2.0.0",
            "3.0.0"
          ),
          false,
          "3.0.0"
        ),
        (
          "non pre-release version",
          List("0.0.1-alpha", "0.0.1"),
          false,
          "0.0.1"
        ),
        (
          "numerically higher pre-release version",
          List("0.0.1-RC.2", "0.0.1-RC.3"),
          false,
          "0.0.1-RC.3"
        ),
        (
          "numerically higher pre-release version even with multiple identifiers",
          List("0.0.1-RC.2", "0.0.1-RC.3"),
          false,
          "0.0.1-RC.3"
        ),
        (
          "lexically higher pre-release version",
          List("0.0.1-RC.1", "0.0.1-RC.2"),
          false,
          "0.0.1-RC.2"
        ),
        ("null when there are no tags", List(), false, null)
      )
    forAll(data) {
      (
        testName: String,
        tags: List[String],
        annotated: Boolean,
        expectedVersion: String
      ) =>
        it(s"latest version is $testName (annotated: $annotated)") {
          new TestSpec {
            override protected def populateRepository(): Unit =
              tags.foreach { tag =>
                testRepository.makeChanges().commitAndTag(tag, annotated)
              }

            override protected def assertion: Assertion =
              SemanticBuildVersion(
                workingDir,
                SemanticBuildVersionConfiguration(tagPrefix = "")
              ).latestVersion.map(_.toStringValue) shouldBe Option(expectedVersion)
          }
        }
    }
  }

  describe("latest version is found through merges") {
    forAll(AnnotatedTestData) { (annotated: Boolean) =>
      it(s" (annotated: $annotated)") {
        new TestSpec {
          override protected def populateRepository(): Unit =
            testRepository
              .makeChanges()
              .commitAndTag("v0.0.1", annotated)
              .makeChanges()
              .commit()
              .branch("feature")
              .checkoutBranch("feature")
              .makeChanges()
              .commit()
              .checkoutBranch("main")
              .makeChanges()
              .commit()
              .merge("feature")

          override protected def assertion: Assertion =
            SemanticBuildVersion(
              workingDir,
              SemanticBuildVersionConfiguration()
            ).latestVersion.map(_.toStringValue) shouldBe Some("0.0.1")
        }
      }
    }
  }

  describe("non sem ver tags are ignored automatically") {
    forAll(AnnotatedTestData) { (annotated: Boolean) =>
      it(s" (annotated: $annotated)") {
        new TestSpec {
          override protected def populateRepository(): Unit =
            testRepository
              .makeChanges()
              .commitAndTag("v0.0.1", annotated)
              .makeChanges()
              .commitAndTag("v0.1", annotated)

          override protected def assertion: Assertion =
            SemanticBuildVersion(
              workingDir,
              SemanticBuildVersionConfiguration(forceBump = true, componentToBump = VersionComponent.PATCH)
            ).determineVersion shouldBe "0.0.2"
        }
      }
    }
  }
}
