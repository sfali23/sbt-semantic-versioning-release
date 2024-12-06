package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.*
import com.alphasystem.sbt.semver.release.test.*
import io.circe.generic.auto.*
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import sbtsemverrelease.VersionsMatching

import scala.util.matching.Regex

class MajorMinorPatchAutobumpingSpec extends AnyFunSuite with TableDrivenPropertyChecks with Matchers {

  import MajorMinorPatchAutobumpingSpec.*

  private val defaultConfiguration = SemanticBuildVersionConfiguration(snapshot = false)

  test(
    "autobumping without any prior commits does not cause build to fail but returns starting version"
  ) {
    new TestSpec {
      override protected def populateRepository(): Unit = ()

      override protected def assertion: Assertion =
        SemanticBuildVersion(
          workingDir,
          defaultConfiguration
        ).determineVersion.toStringValue() shouldBe "v0.1.0"
    }
  }

  forAll(
    DataGenerator.tableFor5(
      getClass.getSimpleName,
      classOf[DataSet].getSimpleName,
      (value: DataSet) => DataSet.unapply(value).get
    )
  ) {
    (
      tagPrefix: Option[String],
      tagNames: List[String],
      autobumpTag: String,
      annotated: Boolean,
      expectedVersion: String
    ) =>
      val tagPrefixStr = tagPrefix.getOrElse("Default")
      val tagNamesStr = tagNames.mkString("[", ", ", "]")
      test(
        s"""test various autobumping variants (tagPrefix: $tagPrefixStr, tagNames $tagNamesStr, autobumpTag: $autobumpTag,
           | annotated: $annotated)"""
          .stripMargin
          .replaceNewLines
      ) {
        new TestSpec {
          override protected def populateRepository(): Unit = {
            tagNames.foreach { tag =>
              testRepository.makeChanges().commitAndTag(tag, annotated)
            }
            testRepository
              .makeChanges()
              .commit(s"""
                         |This is a message
                         |[$autobumpTag]
                         |""".stripMargin)
          }

          override protected def assertion: Assertion = {
            val _tagPrefix = tagPrefix.getOrElse(defaultConfiguration.tagPrefix)
            val config = defaultConfiguration.copy(
              // tagPattern = tagPattern.getOrElse(defaultConfiguration.tagPattern),
              tagPrefix = _tagPrefix
              // versionsMatching = matching.getOrElse(defaultConfiguration.versionsMatching)
            )
            SemanticBuildVersion(
              workingDir,
              config
            ).determineVersion.toStringValue(_tagPrefix) shouldBe expectedVersion
          }
        } // end of TestSpec
      } // end of test
  } // end of forAll

  // TODO: implement tests for true and false

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(
      s"consider all commit messages between HEAD and the nearest ancestor tags for autobumping (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commit()
            .branch("foo")
            .checkoutBranch("foo")
            .makeChanges()
            .commit("[major]")
            .tag("1.2.3-pre.4", annotated)
            .makeChanges()
            .commit("[patch]")
            .checkoutBranch("master")
            .makeChanges()
            .commitAndTag("1.0.0", annotated)
            .makeChanges()
            .commit("bump the [minor] version")
            .merge("foo")

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration
          ).determineVersion shouldBe "1.3.0"
      }
    }
  }

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(
      s"autobumping patch version without major pattern should work properly (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("1.2.2", annotated)
            .makeChanges()
            .commit()
            .makeChanges()
            .commit("[patch]")
            .makeChanges()
            .commit()

        override protected def assertion: Assertion = {
          val autoBump = defaultConfiguration.autoBump.copy(majorPattern = None)
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(autoBump = autoBump)
          ).determineVersion shouldBe "1.2.3"
        }
      }
    }
  }

  // TODO: multiline test
}

object MajorMinorPatchAutobumpingSpec {

  private case class DataSet(
    tagPrefix: Option[String] = None,
    tagNames: List[String] = Nil,
    autobumpTag: String = "",
    annotated: Boolean = false,
    expectedVersion: String = "")
}
