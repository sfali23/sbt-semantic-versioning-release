package com.alphasystem
package sbt
package semver
package release
package internal

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sbtsemverrelease.PreReleaseConfig

class VersionSpec extends AnyWordSpec with Matchers {

  private val preReleaseConfig = PreReleaseConfig(preReleasePartPattern = "(RC)(.)([1-9]\\d*)")

  "Version parser" should {

    "parse simple version" in {
      Version("v0.12.345", PreReleaseConfig()) shouldBe Version(0, 12, 345)
    }

    "parse hot fix version" in {
      Version("v1.89.22.1", PreReleaseConfig()) shouldBe Version(1, 89, 22, Some(1))
    }

    "parse pre-release version" in {
      Version("v1.89.22-RC.5", preReleaseConfig) shouldBe Version(
        1,
        89,
        22,
        preRelease = Some(PreReleaseVersion("RC.", 5))
      )
    }
  }

  "Version sort" should {
    "compare simple version" in {
      Seq(
        Version("v0.12.346", preReleaseConfig),
        Version("v0.12.345-RC.2", preReleaseConfig),
        Version("v1.89.22-RC.5", preReleaseConfig),
        Version("v0.12.345", preReleaseConfig),
        Version("v0.12.345-RC.1", preReleaseConfig)
      ).sorted shouldBe Seq(
        Version("v0.12.345", preReleaseConfig),
        Version("v0.12.345-RC.1", preReleaseConfig),
        Version("v0.12.345-RC.2", preReleaseConfig),
        Version("v0.12.346", preReleaseConfig),
        Version("v1.89.22-RC.5", preReleaseConfig)
      )
    }
  }
}
