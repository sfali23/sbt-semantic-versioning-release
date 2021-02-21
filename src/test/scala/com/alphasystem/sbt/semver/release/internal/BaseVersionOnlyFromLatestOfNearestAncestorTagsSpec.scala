package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.{ internal, _ }
import com.alphasystem.sbt.semver.release.test._
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
            SemanticBuildVersionConfiguration(snapshot = false)
          ).determineVersion shouldBe "1.0.1"
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
            .checkoutBranch("master")
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("1.1.0", annotated)
            .merge("2.x")
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("3.0.0", annotated)
            .checkout("master~")

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            internal.SemanticBuildVersionConfiguration(snapshot = false)
          ).determineVersion shouldBe "2.0.1"
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
            .checkoutBranch("master")
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("1.1.0", annotated)
            .merge("2.x")
            .makeChanges()
            .commit()
            .makeChanges()
            .commitAndTag("4.0.0", annotated)
            .checkout("master~")

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            internal.SemanticBuildVersionConfiguration(snapshot = false)
          ).determineVersion shouldBe "2.0.1"
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
            internal.SemanticBuildVersionConfiguration(snapshot = false)
          )
          val caught = intercept[IllegalArgumentException](
            semanticBuildVersion.determineVersion
          )
          val directory = testRepository.repository.getDirectory
          caught.getMessage shouldBe
            s"""Determined version 1.0.1 already exists on another commit in the repository at '$directory'. Check your
               | configuration to ensure that you haven't forgotten to filter out certain tags or versions. You may 
               |also be bumping the wrong component; if so, bump the component that will give you the intended version,
               | or manually create a tag with the intended version on the commit to be released."""
              .stripMargin
              .replaceNewLines
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
