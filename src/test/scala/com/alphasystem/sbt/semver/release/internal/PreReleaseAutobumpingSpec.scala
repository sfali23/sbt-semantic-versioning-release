package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.test.*
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class PreReleaseAutobumpingSpec extends AnyFunSuite with TableDrivenPropertyChecks with Matchers {

  forAll(AnnotatedTestData) { (annotated: Boolean) =>
    test(s"promoting pre-release version (annotated: $annotated)") {
      new TestSpec {
        override protected def populateRepository(): Unit =
          testRepository
            .makeChanges()
            .commitAndTag("beta.0.2.0", annotated)
            .makeChanges()
            .commitAndTag("beta.0.2.1-RC.1", annotated)
            .makeChanges()
            .commitAndTag("beta.0.2.1-RC.2", annotated)
            .makeChanges()
            .commit("This is a message to [promote] release")

        override protected def assertion: Assertion =
          SemanticBuildVersion(
            workingDir,
            SemanticBuildVersionConfiguration(tagPrefix = "beta.", snapshot = false)
          ) shouldBe "beta.0.2.1"
      }
    }
  }
}
