package com.alphasystem.sbt.release

import com.alphasystem.sbt.release.test._
import io.circe.generic.auto._
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class MajorMinorPatchBumpingSpec
    extends AnyFunSuite
    with TableDrivenPropertyChecks
    with Matchers {

  import MajorMinorPatchBumpingSpec._

  private val defaultConfiguration = SemanticBuildVersionConfiguration()

  test("version without prior tags is default starting snapshot version") {
    new TestSpec {
      override protected def populateRepository(): Unit = ()

      override protected def assertion: Assertion =
        SemanticBuildVersion(
          workingDir,
          defaultConfiguration
        ).determineVersion shouldBe "0.1.0-SNAPSHOT"
    }
  }

  test("version without prior tags is default starting release version") {
    new TestSpec {
      override protected def populateRepository(): Unit = ()

      override protected def assertion: Assertion =
        SemanticBuildVersion(
          workingDir,
          defaultConfiguration.copy(snapshot = false)
        ).determineVersion shouldBe "0.1.0"
    }
  }

  test("version without prior tags and bumping pre-release is not possible") {
    new TestSpec {
      override protected def populateRepository(): Unit = ()

      override protected def assertion: Assertion = {
        val caught =
          intercept[IllegalArgumentException](
            SemanticBuildVersion(
              workingDir,
              defaultConfiguration.copy(
                snapshot = false,
                componentToBump = VersionComponent.PRE_RELEASE
              )
            ).determineVersion
          )
        caught.getMessage shouldBe
          """Cannot bump pre-release because the latest version is not a pre-release version. 
            |To create a new pre-release version, use newPreRelease instead"""
            .stripMargin
            .replaceNewLines
      }
    }
  }

  forAll(
    DataGenerator
      .tableFor3(
        getClass.getSimpleName,
        classOf[DataSet1].getSimpleName,
        DataSet1.Headers,
        (value: DataSet1) => DataSet1.unapply(value).get
      )
  ) {
    (
      startingVersion: String,
      bump: VersionComponent,
      expectedVersion: String
    ) =>
      test(
        s"""without prior tags and with startingVersion "$startingVersion" and bump "$bump" resulting version should be
           | "$expectedVersion"""".stripMargin.replaceNewLines
      ) {
        new TestSpec {
          override protected def populateRepository(): Unit = ()

          override protected def assertion: Assertion = {
            val config = defaultConfiguration
              .copy(
                startingVersion = startingVersion,
                snapshot = false,
                componentToBump = bump
              )
            SemanticBuildVersion(
              workingDir,
              config
            ).determineVersion shouldBe expectedVersion
          }
        } // end of TestSpec
      } // end of test
  } // end of forAll

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(
      s"version without matching tags is default starting snapshot version (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("foo-0.1.0", annotated)

        override protected def assertion: Assertion = {
          val config = defaultConfiguration
            .copy(
              tagPattern = "^bar-".r,
              tagPrefix = "bar-"
            )
          SemanticBuildVersion(
            workingDir,
            config
          ).determineVersion shouldBe "0.1.0-SNAPSHOT"
        }
      }
    }
    //
    test(
      s"version without matching tags is default starting release version (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("foo-0.1.0", annotated)

        override protected def assertion: Assertion = {
          val config = defaultConfiguration
            .copy(
              tagPattern = "^bar-".r,
              tagPrefix = "bar-",
              snapshot = false
            )
          SemanticBuildVersion(
            workingDir,
            config
          ).determineVersion shouldBe "0.1.0"
        }
      }
    }
    //
    test(
      s"version with prior tag and uncommitted changes is next snapshot version (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .commitAndTag("v0.0.1", annotated)
            .makeChanges()

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration
          ).determineVersion shouldBe "0.0.2-SNAPSHOT"
      }
    }
    //
    test(
      s"version with prior tag and committed changes is next release version (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .commitAndTag("v0.0.1", annotated)
            .makeChanges()
            .commit()

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(snapshot = false)
          ).determineVersion shouldBe "0.0.2"
      }
    }
    //
    test(
      s"checking out tag produces same version as tag (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .commitAndTag("v3.1.2", annotated)
            .commitAndTag("v3.1.3", annotated)
            .commitAndTag("v3.1.4", annotated)
            .checkout("v3.1.2")

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(snapshot = false)
          ).determineVersion shouldBe "3.1.2"
      }
    }
    //
    test(
      s"checking out tag produces same version as tag even if other tags are present (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .commitAndTag("v3.1.2", annotated)
            .commitAndTag("v3.1.3", annotated)
            .commitAndTag("v3.1.4", annotated)
            .checkout("v3.1.2")
            .tag("foo", annotated)

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(snapshot = false)
          ).determineVersion shouldBe "3.1.2"
      }
    }
  }

  test(
    "creating release version with uncommitted changes causes build to fail"
  ) {
    new TestSpec {
      override protected def populateRepository(): Unit =
        testRepository.makeChanges()

      override protected def assertion: Assertion = {
        val config = defaultConfiguration.copy(snapshot = false)
        val caught = intercept[IllegalArgumentException](
          SemanticBuildVersion(workingDir, config).determineVersion
        )
        caught.getMessage shouldBe "Cannot create a release version when there are uncommitted changes"
      }
    }
  }

  test(
    "version with custom snapshot suffix"
  ) {
    new TestSpec {
      override protected def populateRepository(): Unit =
        testRepository.makeChanges()

      override protected def assertion: Assertion = {
        val config = defaultConfiguration.copy(snapshotSuffix = "CURRENT")
        SemanticBuildVersion(
          workingDir,
          config
        ).determineVersion shouldBe "0.1.0-CURRENT"
      }
    }
  }

  forAll(
    DataGenerator
      .tableFor4(
        getClass.getSimpleName,
        classOf[DataSet2].getSimpleName,
        DataSet2.Headers,
        (value: DataSet2) => DataSet2.unapply(value).get
      )
  ) {
    (
      bump: VersionComponent,
      `type`: String,
      annotated: Boolean,
      expectedVersion: String
    ) =>
      test(
        s"bumping ${bump.name().toLowerCase} version for ${`type`} (annotated: $annotated)"
      ) {
        {
          new TestSpec {
            override protected def populateRepository(): Unit =
              testRepository
                .commitAndTag("v0.0.2", annotated)
                .makeChanges()
                .commit()

            override protected def assertion: Assertion =
              SemanticBuildVersion(
                workingDir,
                defaultConfiguration.copy(
                  snapshot = `type` == "snapshot",
                  componentToBump = bump
                )
              ).determineVersion shouldBe expectedVersion
          }
        }
      }
      //
      test(
        s"bumping ${bump.name().toLowerCase} version with tag pattern for ${`type`} (annotated: $annotated)"
      ) {
        {
          val snapshot = `type` == "snapshot"
          new TestSpec {
            override protected def populateRepository(): Unit = {
              testRepository
                .commitAndTag("foo-0.1.1", annotated)
                .commitAndTag("foo-0.1.2", annotated)
                .commitAndTag("bar-0.0.1", annotated)
                .commitAndTag("bar-0.0.2", annotated)
                .makeChanges()
              if (snapshot) {
                testRepository.makeChanges()
              } else {
                testRepository.commit()
              }
            }

            override protected def assertion: Assertion =
              SemanticBuildVersion(
                workingDir,
                defaultConfiguration.copy(
                  snapshot = snapshot,
                  componentToBump = bump,
                  tagPattern = "^bar-".r,
                  tagPrefix = "bar-"
                )
              ).determineVersion shouldBe expectedVersion
          }
        }
      } //
  }

  forAll(
    DataGenerator
      .tableFor6(
        getClass.getSimpleName,
        classOf[DataSet3].getSimpleName,
        DataSet3.Headers,
        (value: DataSet3) => DataSet3.unapply(value).get
      )
  ) {
    (
      bump: VersionComponent,
      `type`: String,
      matching: VersionsMatching,
      tagNames: List[String],
      annotated: Boolean,
      expectedVersion: String
    ) =>
      test(
        s"bumping ${bump.name().toLowerCase} version with versions matching for ${`type`} (annotated: $annotated)"
      ) {
        {
          val snapshot = `type` == "snapshot"
          new TestSpec {
            override protected def populateRepository(): Unit = {
              tagNames.foreach { tag =>
                testRepository.commitAndTag(tag, annotated)
              }
              if (snapshot) {
                testRepository.makeChanges()
              } else {
                testRepository.commit()
              }
            }

            override protected def assertion: Assertion =
              SemanticBuildVersion(
                workingDir,
                defaultConfiguration.copy(
                  snapshot = snapshot,
                  componentToBump = bump,
                  versionsMatching = matching
                )
              ).determineVersion shouldBe expectedVersion
          }
        }
      }
  }
}

object MajorMinorPatchBumpingSpec {

  private case class DataSet1(
    startingVersion: String = "",
    bump: VersionComponent = VersionComponent.NONE,
    expectedVersion: String = "")

  private object DataSet1 {

    val Headers: (String, String, String) = (
      "startingVersion",
      "bump",
      "expectedVersion"
    )
  }

  private case class DataSet2(
    bump: VersionComponent = VersionComponent.NONE,
    `type`: String = "",
    annotated: Boolean = false,
    expectedVersion: String = "")

  private object DataSet2 {

    val Headers: (String, String, String, String) =
      ("bump", "type", "annotated", "expectedVersion")
  }

  private case class DataSet3(
    bump: VersionComponent = VersionComponent.NONE,
    `type`: String = "",
    matching: VersionsMatching = VersionsMatching(),
    tagNames: List[String] = Nil,
    annotated: Boolean = false,
    expectedVersion: String = "")

  private object DataSet3 {

    val Headers: (String, String, String, String, String, String) = (
      "bump",
      "type",
      "matching",
      "tagNames",
      "annotated",
      "expectedVersion"
    )
  }
}
