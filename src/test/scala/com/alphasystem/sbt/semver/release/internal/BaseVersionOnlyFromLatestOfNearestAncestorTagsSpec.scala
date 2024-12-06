package com.alphasystem
package sbt
package semver
package release
package internal

import com.alphasystem.sbt.semver.release.test.*
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class BaseVersionOnlyFromLatestOfNearestAncestorTagsSpec
    extends AnyFunSuite
    with TableDrivenPropertyChecks
    with Matchers {

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(
      s"take the latest ancestor tag, ignoring other tags (annotated: $annotated) - v1"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("1.0.0", annotated)
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("2.0.0", annotated)
            .checkout("HEAD~")

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            SemanticBuildVersionConfiguration(snapshot = false, tagPrefix = "")
          ).determineVersion.toStringValue("") shouldBe "1.0.1"
      }
    }
  }

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(
      s"take the latest ancestor tag, ignoring other tags (annotated: $annotated) - v2"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("1.0.0", annotated)
            .branch("2.x")
            .checkoutBranch("2.x")
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("2.0.0", annotated)
            .makeChanges()
            .commit()
            .makeChanges()
            .commit()
            .checkoutBranch("main")
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("1.1.0", annotated)
            .merge("2.x")
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("3.0.0", annotated)
            .checkout("main~")

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            SemanticBuildVersionConfiguration(snapshot = false, tagPrefix = "")
          ).determineVersion.toStringValue("") shouldBe "2.0.1"
      }
    }
  }

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(
      s"take the latest tag from the nearest ancestor tags, ignoring unrelated tags (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("3.0.0", annotated)
            .makeChanges()
            .commitAndTag("1.0.0", annotated)
            .branch("2.x")
            .checkoutBranch("2.x")
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("2.0.0", annotated)
            .makeChanges()
            .commit()
            .makeChanges()
            .commit()
            .checkoutBranch("main")
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("1.1.0", annotated)
            .merge("2.x")
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("4.0.0", annotated)
            .checkout("main~")

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            SemanticBuildVersionConfiguration(snapshot = false, tagPrefix = "")
          ).determineVersion.toStringValue("") shouldBe "3.0.1"
      }
    }
  }

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(
      s"if the determined version exists already as tag, the build should fail (annotated: $annotated) - v1"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("1.0.0", annotated)
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("1.0.1", annotated)
            .checkout("HEAD~")

        override protected def assertion: Assertion = {
          val semanticBuildVersion = SemanticBuildVersion(
            workingDir,
            SemanticBuildVersionConfiguration(snapshot = false, tagPrefix = "")
          )
          println(s">>>> ${semanticBuildVersion.determineVersion.toStringValue("")}")
          val caught = intercept[IllegalArgumentException](semanticBuildVersion.determineVersion)
          caught.getMessage shouldBe "Couldn't determine next version, tag (v1.0.0) is already exists"
        }
      }
    }
  }

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(
      s"if the determined version exists already as tag, a snapshot build should not fail (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("1.0.0", annotated)
            .commit()
            .makeChanges()
            .commitAndTag("1.0.1", annotated)
            .checkout("HEAD~")

        override protected def assertion: Assertion = {
          val semanticBuildVersion = SemanticBuildVersion(workingDir)
          semanticBuildVersion.determineVersion shouldBe "1.0.1-SNAPSHOT"
        }
      }
    }
  }
}
