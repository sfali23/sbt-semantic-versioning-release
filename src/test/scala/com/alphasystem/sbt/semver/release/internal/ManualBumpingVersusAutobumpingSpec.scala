package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release._
import com.alphasystem.sbt.semver.release.test._
import io.circe.generic.auto._
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import sbtsemverrelease.PreReleaseConfig

class ManualBumpingVersusAutobumpingSpec
    extends AnyFunSuite
    with TableDrivenPropertyChecks
    with Matchers {

  import ManualBumpingVersusAutobumpingSpec._

  private val defaultConfiguration =
    SemanticBuildVersionConfiguration(
      snapshot = false,
      preReleaseConfig = PreReleaseConfig(startingVersion = "pre.1")
    )

  forAll(
    DataGenerator
      .tableFor8(
        getClass.getSimpleName,
        classOf[DataSet1].getSimpleName,
        (value: DataSet1) => DataSet1.unapply(value).get
      )
  ) {
    (
      autobumpTag: Option[String],
      preReleaseTag: Option[String],
      bump: VersionComponent,
      newPreRelease: Boolean,
      promoteToRelease: Boolean,
      forceBump: Boolean,
      annotated: Boolean,
      expectedVersion: String
    ) =>
      val autobumpTagMsg = autobumpTag.map("[" + _ + "]").getOrElse("[]")
      val preReleaseTagMsg = preReleaseTag.map("[" + _ + "]").getOrElse("[]")
      test(
        s"""autobumping $autobumpTagMsg version, preReleaseTag $preReleaseTagMsg, manually bumping $bump version, 
           |manual newPreRelease $newPreRelease, manual promoteToRelease $promoteToRelease, forceBump $forceBump 
           |expectedVersion: $expectedVersion (annotated: $annotated)"""
          .stripMargin
          .replaceNewLines
      ) {
        new TestSpec {
          override protected def populateRepository(): Unit =
            testRepository
              .makeChanges()
              .commitAndTag("1.2.3-pre.4", annotated)
              .makeChanges()
              .commit(
                s"This is a message $autobumpTagMsg $preReleaseTagMsg"
              )

          override protected def assertion: Assertion = {
            val config = defaultConfiguration.copy(
              forceBump = forceBump,
              newPreRelease = newPreRelease,
              promoteToRelease = promoteToRelease,
              componentToBump = bump
            )
            SemanticBuildVersion(
              workingDir,
              config
            ).determineVersion shouldBe expectedVersion
          }
        }
      }
  }

  forAll(
    DataGenerator
      .tableFor8(
        getClass.getSimpleName,
        classOf[DataSet2].getSimpleName,
        (value: DataSet2) => DataSet2.unapply(value).get
      )
  ) {
    (
      autobumpTag: Option[String],
      preReleaseTag: Option[String],
      bump: VersionComponent,
      newPreRelease: Boolean,
      promoteToRelease: Boolean,
      forceBump: Boolean,
      annotated: Boolean,
      expectedExceptionMessage: String
    ) =>
      val autobumpTagMsg = autobumpTag.map("[" + _ + "]").getOrElse("[]")
      val preReleaseTagMsg = preReleaseTag.map("[" + _ + "]").getOrElse("[]")
      test(
        s"""autobumping $autobumpTagMsg version, preReleaseTag $preReleaseTagMsg, manually bumping $bump version, 
           |manual newPreRelease $newPreRelease, manual promoteToRelease $promoteToRelease, forceBump $forceBump - 
           |should fail (annotated: $annotated)""".stripMargin.replaceNewLines
      ) {
        new TestSpec {
          override protected def populateRepository(): Unit =
            testRepository
              .makeChanges()
              .commitAndTag("1.2.3-pre.4", annotated)
              .makeChanges()
              .commit(
                s"This is a message $autobumpTagMsg $preReleaseTagMsg"
              )

          override protected def assertion: Assertion = {
            val config = defaultConfiguration.copy(
              forceBump = forceBump,
              newPreRelease = newPreRelease,
              promoteToRelease = promoteToRelease,
              componentToBump = bump
            )
            val caught =
              intercept[IllegalArgumentException](
                SemanticBuildVersion(
                  workingDir,
                  config
                ).determineVersion
              )

            caught.getMessage shouldBe expectedExceptionMessage
          }
        }
      }
  }
}

object ManualBumpingVersusAutobumpingSpec {

  private case class DataSet1(
    autobumpTag: Option[String] = None,
    preReleaseTag: Option[String] = None,
    bump: VersionComponent = VersionComponent.NONE,
    newPreRelease: Boolean = false,
    promoteToRelease: Boolean = false,
    forceBump: Boolean = false,
    annotated: Boolean = false,
    expectedVersion: String = "")

  private case class DataSet2(
    autobumpTag: Option[String] = None,
    preReleaseTag: Option[String] = None,
    bump: VersionComponent = VersionComponent.NONE,
    newPreRelease: Boolean = false,
    promoteToRelease: Boolean = false,
    forceBump: Boolean = false,
    annotated: Boolean = false,
    expectedExceptionMessage: String = "")
}
