package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.internal
import com.alphasystem.sbt.semver.release.test._
import org.scalatest.Assertion
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class SemanticBuildVersionSpec
    extends AnyFunSpec
    with TableDrivenPropertyChecks
    with Matchers {

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
          List("0.0.1-2", "0.0.1-3"),
          false,
          "0.0.1-3"
        ),
        (
          "numerically higher pre-release version even with multiple identifiers",
          List("0.0.1-alpha.2", "0.0.1-alpha.3"),
          false,
          "0.0.1-alpha.3"
        ),
        (
          "lexically higher pre-release version",
          List("0.0.1-x", "0.0.1-y"),
          false,
          "0.0.1-y"
        ),
        (
          "lexically higher pre-release version even with multiple identifiers",
          List("0.0.1-alpha.x", "0.0.1-alpha.y"),
          false,
          "0.0.1-alpha.y"
        ),
        (
          "non numeric pre-release version",
          List("0.0.1-999", "0.0.1-alpha"),
          false,
          "0.0.1-alpha"
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
                SemanticBuildVersionConfiguration()
              ).latestVersion shouldBe Option(expectedVersion)
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
              .commitAndTag("0.0.1", annotated)
              .makeChanges()
              .commit()
              .branch("feature")
              .checkoutBranch("feature")
              .makeChanges()
              .commit()
              .checkoutBranch("master")
              .makeChanges()
              .commit()
              .merge("feature")

          override protected def assertion: Assertion =
            SemanticBuildVersion(
              workingDir,
              internal.SemanticBuildVersionConfiguration()
            ).latestVersion shouldBe Some("0.0.1")
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
              internal.SemanticBuildVersionConfiguration()
            ).determineVersion shouldBe "0.0.2-SNAPSHOT"
        }
      }
    }
  }

  describe("tagged version is recognized as non snapshot") {
    forAll(AnnotatedTestData) { (annotated: Boolean) =>
      it(s" (annotated: $annotated)") {
        new TestSpec {
          override protected def populateRepository(): Unit =
            testRepository
              .makeChanges()
              .commitAndTag("v0.0.1", annotated)

          override protected def assertion: Assertion = {
            val sbv = SemanticBuildVersion(
              workingDir,
              internal.SemanticBuildVersionConfiguration()
            )
            sbv.determineVersion
            sbv.currentConfig.snapshot shouldBe false
          }
        }
      }
    }
  }
}
