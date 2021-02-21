package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release._
import com.alphasystem.sbt.semver.release.test._
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class PreReleaseBumpingSpec
    extends AnyFunSuite
    with TableDrivenPropertyChecks
    with Matchers {

  private val defaultConfiguration =
    SemanticBuildVersionConfiguration(
      componentToBump = VersionComponent.PRE_RELEASE,
      tagPrefix = "alpha."
    )

  test(
    "bumped pre-release snapshot version without prior pre release version causes build to fail"
  ) {
    new TestSpec {
      override protected def assertion: Assertion = {
        val caught =
          intercept[IllegalArgumentException](
            SemanticBuildVersion(
              workingDir,
              defaultConfiguration
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

  test(
    "bumped pre-release version without prior pre release version causes build to fail"
  ) {
    new TestSpec {
      override protected def assertion: Assertion = {
        val caught =
          intercept[IllegalArgumentException](
            SemanticBuildVersion(
              workingDir,
              defaultConfiguration.copy(snapshot = false)
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

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(
      s"bumped pre-release version with prior non pre release version causes build to fail (annotated: $annotated)"
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commit()

        override protected def assertion: Assertion = {
          val caught =
            intercept[IllegalArgumentException](
              SemanticBuildVersion(
                workingDir,
                defaultConfiguration.copy(snapshot = false)
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
    //
    test(
      s"""bumped pre-release version with prior pre release version is bumped pre release version for 
         |snapshot (annotated: $annotated)""".stripMargin.replaceNewLines
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commitAndTag("0.2.1-alpha.1", annotated)
            .makeChanges()

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration
          ).determineVersion shouldBe "0.2.1-alpha.2-SNAPSHOT"
      }
    }
    //
    test(
      s"""bumped pre-release version with prior pre release version is bumped pre release version 
         |(annotated: $annotated)""".stripMargin.replaceNewLines
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commitAndTag("0.2.1-alpha.1", annotated)
            .makeChanges()
            .commit()

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(snapshot = false)
          ).determineVersion shouldBe "0.2.1-alpha.2"
      }
    }
    //
    test(
      s"""implicitly bumped pre-release version with prior pre release version is bumped pre release version for
         | snapshot (annotated: $annotated)""".stripMargin.replaceNewLines
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commitAndTag("0.2.1-alpha.1", annotated)
            .makeChanges()
            .commit()

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(componentToBump = VersionComponent.NONE)
          ).determineVersion shouldBe "0.2.1-alpha.2-SNAPSHOT"
      }
    }
    //
    test(
      s"""implicitly bumped pre-release version with prior pre release version is bumped pre release version  
         |(annotated: $annotated)""".stripMargin
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commitAndTag("0.2.1-alpha.1", annotated)
            .makeChanges()
            .commit()

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(
              snapshot = false,
              componentToBump = VersionComponent.NONE
            )
          ).determineVersion shouldBe "0.2.1-alpha.2"
      }
    }
    //
    test(
      s"""with pattern bumped pre-release snapshot version with prior non pre release version causes build to
         | fail (annotated: $annotated)""".stripMargin.replaceNewLines
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()

        override protected def assertion: Assertion = {
          val caught =
            intercept[IllegalArgumentException](
              SemanticBuildVersion(
                workingDir,
                defaultConfiguration.copy(
                  tagPattern = "beta".r,
                  tagPrefix = "beta"
                )
              ).determineVersion
            )
          caught.getMessage shouldBe
            """Cannot bump pre-release because the latest version is not a pre-release version.
              | To create a new pre-release version, use newPreRelease instead"""
              .stripMargin
              .replaceNewLines
        }
      }
    }
    //
    test(
      s"""with pattern bumped pre-release version with prior non pre release version causes build to
         | fail (annotated: $annotated)""".stripMargin.replaceNewLines
    ) {
      new TestSpec {
        override protected def assertion: Assertion = {
          val caught =
            intercept[IllegalArgumentException](
              SemanticBuildVersion(
                workingDir,
                defaultConfiguration.copy(
                  tagPattern = "beta".r,
                  tagPrefix = "beta",
                  snapshot = false
                )
              ).determineVersion
            )
          caught.getMessage shouldBe
            """Cannot bump pre-release because the latest version is not a pre-release version.
              | To create a new pre-release version, use newPreRelease instead"""
              .stripMargin
              .replaceNewLines
        }
      }
    }
    //
    test(
      s"""bumped pre-release snapshot version with prior non pre release version causes build to fail 
         |(annotated: $annotated)""".stripMargin
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commit()

        override protected def assertion: Assertion = {
          val caught =
            intercept[IllegalArgumentException](
              SemanticBuildVersion(
                workingDir,
                defaultConfiguration.copy(snapshot = false)
              ).determineVersion
            )
          caught.getMessage shouldBe
            """Cannot bump pre-release because the latest version is not a pre-release version.
              | To create a new pre-release version, use newPreRelease instead"""
              .stripMargin
              .replaceNewLines
        }
      }
    }
    //
    test(
      s"""bumped pre-release version with prior pre release version is bumped pre release version for snapshot 
         |(annotated: $annotated)""".stripMargin
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commitAndTag("0.2.1-alpha.1", annotated)
            .makeChanges()

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration
          ).determineVersion shouldBe "0.2.1-alpha.2-SNAPSHOT"
      }
    }
    //
    test(
      s"""bumped pre-release version with prior pre release version is bumped pre release version 
         |(annotated: $annotated)""".stripMargin
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commitAndTag("0.2.1-alpha.1", annotated)
            .makeChanges()
            .commit()

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(snapshot = false)
          ).determineVersion shouldBe "0.2.1-alpha.2"
      }
    }
    //
    test(
      s"""with pattern bumped pre-release snapshot version with non matching prior pre release version causes 
         |build to fail (annotated: $annotated)""".stripMargin.replaceNewLines
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commitAndTag("0.2.1-alpha.1", annotated)
            .makeChanges()

        override protected def assertion: Assertion = {
          val caught =
            intercept[IllegalArgumentException](
              SemanticBuildVersion(
                workingDir,
                defaultConfiguration.copy(
                  tagPattern = "beta".r,
                  tagPrefix = "beta"
                )
              ).determineVersion
            )
          caught.getMessage shouldBe
            """Cannot bump pre-release because the latest version is not a pre-release version.
              | To create a new pre-release version, use newPreRelease instead"""
              .stripMargin
              .replaceNewLines
        }
      }
    }
    //
    test(
      s"""with pattern bumped pre-release version with non matching prior pre release version causes build to fail
         | (annotated: $annotated)""".stripMargin.replaceNewLines
    ) {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("0.2.0", annotated)
            .makeChanges()
            .commitAndTag("0.2.1-alpha.1", annotated)
            .makeChanges()
            .commit()

        override protected def assertion: Assertion = {
          val caught =
            intercept[IllegalArgumentException](
              SemanticBuildVersion(
                workingDir,
                defaultConfiguration.copy(
                  tagPattern = "beta".r,
                  tagPrefix = "beta",
                  snapshot = false
                )
              ).determineVersion
            )
          caught.getMessage shouldBe
            """Cannot bump pre-release because the latest version is not a pre-release version.
              | To create a new pre-release version, use newPreRelease instead"""
              .stripMargin
              .replaceNewLines
        }
      }
    }
    //
    test(
      s"""with pattern bumped pre-release version with prior pre release version is bumped pre release version for
         | snapshot (annotated: $annotated)""".stripMargin.replaceNewLines
    ) {
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

        override protected def assertion: Assertion = {
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(
              tagPattern = "beta".r,
              tagPrefix = "beta"
            )
          ).determineVersion shouldBe "0.2.1-beta.2-SNAPSHOT"
        }
      }
    }
    //
    test(
      s"""with pattern bumped pre-release version with prior pre release version is bumped pre release version
         | (annotated: $annotated)"""
        .stripMargin
        .replaceNewLines
    ) {
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
            .commit()

        override protected def assertion: Assertion = {
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(
              tagPattern = "beta".r,
              tagPrefix = "beta",
              snapshot = false
            )
          ).determineVersion shouldBe "0.2.1-beta.2"
        }
      }
    }
    //
    test(
      s"""with pattern implicitly bumped pre-release version with prior pre release version is bumped pre release
         | version for snapshot (annotated: $annotated)"""
        .stripMargin
        .replaceNewLines
    ) {
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

        override protected def assertion: Assertion = {
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(
              tagPattern = "beta".r,
              tagPrefix = "beta",
              componentToBump = VersionComponent.NONE
            )
          ).determineVersion shouldBe "0.2.1-beta.2-SNAPSHOT"
        }
      }
    }
    //
    test(
      s"""with pattern implicitly bumped pre-release version with prior pre release version is bumped pre release
         | version (annotated: $annotated)"""
        .stripMargin
        .replaceNewLines
    ) {
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
            .commit()

        override protected def assertion: Assertion = {
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(
              tagPattern = "beta".r,
              tagPrefix = "beta",
              snapshot = false,
              componentToBump = VersionComponent.NONE
            )
          ).determineVersion shouldBe "0.2.1-beta.2"
        }
      }
    }
    //
    test(
      s"promoting pre-release version with snapshot (annotated: $annotated)"
    ) {
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

        override protected def assertion: Assertion = {
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(
              tagPattern = "beta".r,
              tagPrefix = "beta",
              componentToBump = VersionComponent.NONE,
              promoteToRelease = true
            )
          ).determineVersion shouldBe "0.2.1-SNAPSHOT"
        }
      }
    }
    //
    test(
      s"promoting pre-release version (annotated: $annotated)"
    ) {
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
            .commit()

        override protected def assertion: Assertion = {
          SemanticBuildVersion(
            workingDir,
            defaultConfiguration.copy(
              tagPattern = "beta".r,
              tagPrefix = "beta",
              componentToBump = VersionComponent.NONE,
              promoteToRelease = true,
              snapshot = false
            )
          ).determineVersion shouldBe "0.2.1"
        }
      }
    }
  }

  test(
    "with pattern bumped pre-release version without prior pre release version causes build to fail"
  ) {
    new TestSpec {
      override protected def assertion: Assertion = {
        val caught =
          intercept[IllegalArgumentException](
            SemanticBuildVersion(
              workingDir,
              defaultConfiguration.copy(
                tagPattern = "beta".r,
                tagPrefix = "beta",
                snapshot = false
              )
            ).determineVersion
          )
        caught.getMessage shouldBe
          """Cannot bump pre-release because the latest version is not a pre-release version.
            | To create a new pre-release version, use newPreRelease instead"""
            .stripMargin
            .replaceNewLines
      }
    }
  }

  test(
    "with pattern bumped pre-release snapshot version without prior pre release version causes build to fail"
  ) {
    new TestSpec {
      override protected def assertion: Assertion = {
        val caught =
          intercept[IllegalArgumentException](
            SemanticBuildVersion(
              workingDir,
              defaultConfiguration.copy(
                tagPattern = "beta".r,
                tagPrefix = "beta"
              )
            ).determineVersion
          )
        caught.getMessage shouldBe
          """Cannot bump pre-release because the latest version is not a pre-release version.
            | To create a new pre-release version, use newPreRelease instead"""
            .stripMargin
            .replaceNewLines
      }
    }
  }
}
