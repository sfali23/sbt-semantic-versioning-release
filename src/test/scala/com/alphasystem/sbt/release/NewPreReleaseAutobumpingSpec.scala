package com.alphasystem.sbt.release

import com.alphasystem.sbt.release.test.DataGenerator
import io.circe.generic.auto._
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class NewPreReleaseAutobumpingSpec
    extends AnyFunSuite
    with TableDrivenPropertyChecks
    with Matchers {

  import NewPreReleaseAutobumpingSpec._

  private val defaultConfiguration =
    SemanticBuildVersionConfiguration(
      snapshot = false,
      preReleasePrefix = "pre."
    )

  test(
    "new pre-release autobumping with promote to release causes build to fail"
  ) {
    new TestSpec {
      override protected def populateRepository(): Unit =
        testRepository
          .makeChanges()
          .commit("This is a message [new-pre-release] [promote]")

      override protected def assertion: Assertion = {
        val caught =
          intercept[IllegalArgumentException](
            SemanticBuildVersion(
              workingDir,
              defaultConfiguration
            ).determineVersion
          )
        caught.getMessage shouldBe "Creating a new pre-release while also promoting a pre-release is not supported"
      }
    }
  }

  forAll(
    DataGenerator
      .tableFor5(
        classOf[NewPreReleaseAutobumpingSpec].getSimpleName,
        classOf[DataSet].getSimpleName,
        (value: DataSet) => DataSet.unapply(value).get
      )
  ) {
    (
      startingVersion: String,
      tagNames: List[String],
      autobumpTag: Option[String],
      annotated: Boolean,
      expectedVersion: String
    ) =>
      val autobumpTagCommitMsg = autobumpTag.map("[" + _ + "]").getOrElse("[]")
      val autobumpTagMsg =
        if (autobumpTag.isEmpty) "and without explicit bump"
        else s"with bump ${autobumpTag.get}"
      val tagNamesMsg = if (tagNames.isEmpty) "without" else "with"
      test(
        s"""new pre-release autobumping $tagNamesMsg prior versions $autobumpTagMsg, expectedVersion $expectedVersion,
           | startingVersion $startingVersion (annotated: $annotated)"""
          .stripMargin
          .replaceNewLines
      ) {
        new TestSpec {
          override protected def populateRepository(): Unit = {
            tagNames.foreach(tag => testRepository.commitAndTag(tag, annotated))
            testRepository
              .makeChanges()
              .commit(
                s"This is a message [new-pre-release] $autobumpTagCommitMsg"
              )
          }

          override protected def assertion: Assertion =
            SemanticBuildVersion(
              workingDir,
              defaultConfiguration.copy(startingVersion = startingVersion)
            ).determineVersion shouldBe expectedVersion
        }
      }
  }
}

object NewPreReleaseAutobumpingSpec {

  private case class DataSet(
    startingVersion: String = "",
    tagNames: List[String] = Nil,
    autobumpTag: Option[String] = None,
    annotated: Boolean = false,
    expectedVersion: String = "")
}
