package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release._
import com.alphasystem.sbt.semver.release.test._
import io.circe.generic.auto._
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class NewPreReleaseBumpingSpec
    extends AnyFunSuite
    with TableDrivenPropertyChecks
    with Matchers {

  import NewPreReleaseBumpingSpec._

  private val defaultConfiguration =
    SemanticBuildVersionConfiguration(
      preReleasePrefix = "pre.",
      tagPrefix = "",
      newPreRelease = true
    )

  forAll(
    DataGenerator
      .tableFor6(
        classOf[NewPreReleaseBumpingSpec].getSimpleName,
        classOf[DataSet1].getSimpleName,
        (value: DataSet1) => DataSet1.unapply(value).get
      )
  ) {
    (
      startingVersion: String,
      tagNames: List[String],
      bumpComponent: VersionComponent,
      snapshot: Boolean,
      annotated: Boolean,
      expectedVersion: String
    ) =>
      val snapshotMsg = if (snapshot) "snapshot " else ""
      val with_without_msg = if (tagNames.nonEmpty) "with" else "without"
      val preReleaseMsg = tagNames
        .collectFirst {
          case tagName if tagName.endsWith("-pre.1") => "pre-release "
        }
        .getOrElse("")
      val bumpMsg1 =
        if (bumpComponent.isNone) ""
        else s" with bump ${bumpComponent.name().toLowerCase}"
      val bumpMsg2 = if (bumpComponent.isNone) " with " else " and "
      test(
        s"""${snapshotMsg}new pre-release version $with_without_msg prior ${preReleaseMsg}version$bumpMsg1$bumpMsg2
        startingVersion $startingVersion (annotated: $annotated)"""
          .stripMargin
          .replaceNewLines
      ) {
        new TestSpec {
          override protected def populateRepository(): Unit = {
            tagNames.foreach(tag =>
              testRepository.makeChanges().commitAndTag(tag, annotated)
            )
            testRepository
              .makeChanges()
              .commit()
          }

          override protected def assertion: Assertion =
            SemanticBuildVersion(
              workingDir,
              defaultConfiguration.copy(
                componentToBump = bumpComponent,
                startingVersion = startingVersion,
                snapshot = snapshot
              )
            ).determineVersion shouldBe expectedVersion
        }
      }
  }

  forAll(
    DataGenerator
      .tableFor3(
        classOf[NewPreReleaseBumpingSpec].getSimpleName,
        classOf[DataSet2].getSimpleName,
        (value: DataSet2) => DataSet2.unapply(value).get
      )
  ) {
    (
      snapshot: Boolean,
      annotated: Boolean,
      expectedVersion: String
    ) =>
      test(
        s"""new pre-release version with snapshot '$snapshot' without matching tags (annotated: $annotated)""".stripMargin
      ) {
        new TestSpec {
          override protected def populateRepository(): Unit = {
            testRepository
              .makeChanges()
              .commitAndTag("foo-0.1.0", annotated)
          }

          override protected def assertion: Assertion =
            SemanticBuildVersion(
              workingDir,
              defaultConfiguration.copy(
                snapshot = snapshot,
                tagPattern = "^bar-".r,
                tagPrefix = "bar-"
              )
            ).determineVersion shouldBe expectedVersion
        }
      }
  }
}

object NewPreReleaseBumpingSpec {

  private case class DataSet1(
    startingVersion: String = "",
    tagNames: List[String] = Nil,
    bumpComponent: VersionComponent = VersionComponent.NONE,
    snapshot: Boolean = false,
    annotated: Boolean = false,
    expectedVersion: String = "")

  private case class DataSet2(
    snapshot: Boolean = false,
    annotated: Boolean = false,
    expectedVersion: String = "")
}
