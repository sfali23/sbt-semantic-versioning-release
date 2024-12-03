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
      Version("0.12.345", PreReleaseConfig()) shouldBe Version(0, 12, 345)
    }

    "parse hot fix version" in {
      Version("1.89.22.1", PreReleaseConfig()) shouldBe Version(1, 89, 22, Some(1))
    }

    "parse pre-release version" in {
      Version("1.89.22-RC.5", preReleaseConfig) shouldBe Version(
        1,
        89,
        22,
        preRelease = Some(PreReleaseVersion("RC.", 5)),
        preReleaseConfig = preReleaseConfig
      )
    }
  }

  "Version sort" should {
    "compare simple version" in {
      Seq(
        Version("0.12.346", preReleaseConfig),
        Version("0.12.345-RC.2", preReleaseConfig),
        Version("1.89.22-RC.5", preReleaseConfig),
        Version("0.12.345", preReleaseConfig),
        Version("0.12.345-RC.1", preReleaseConfig)
      ).sorted shouldBe Seq(
        Version("0.12.345", preReleaseConfig),
        Version("0.12.345-RC.1", preReleaseConfig),
        Version("0.12.345-RC.2", preReleaseConfig),
        Version("0.12.346", preReleaseConfig),
        Version("1.89.22-RC.5", preReleaseConfig)
      )
    }
  }

  "Version" should {

    "bump patch version" in {
      val expectedVersion = Version(major = 0, minor = 1, patch = 0, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpPatch shouldBe expectedVersion.copy(patch = 1)
    }

    "bump minor version" in {
      val expectedVersion = Version(major = 0, minor = 1, patch = 3, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpMinor shouldBe expectedVersion.copy(minor = 2, patch = 0)
    }

    "bump major version" in {
      val expectedVersion = Version(major = 0, minor = 5, patch = 23, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpMajor shouldBe expectedVersion.copy(major = 1, minor = 0, patch = 0)
    }

    "bump hotfix version" in {
      val expectedVersion = Version(major = 1, minor = 2, patch = 7, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpHotfix shouldBe expectedVersion.copy(hotfix = Some(1))
    }

    "create new pre-release version with patch version bump" in {
      val expectedVersion = Version(major = 2, minor = 0, patch = 17, preReleaseConfig = preReleaseConfig)
      expectedVersion.newPreRelease(VersionComponent.PATCH) shouldBe expectedVersion.copy(
        patch = 18,
        preRelease = Some(PreReleaseVersion("RC.", 1))
      )
    }

    "create new pre-release version with minor version bump" in {
      val expectedVersion = Version(major = 2, minor = 0, patch = 17, preReleaseConfig = preReleaseConfig)
      expectedVersion.newPreRelease(VersionComponent.MINOR) shouldBe expectedVersion.copy(
        minor = 1,
        patch = 0,
        preRelease = Some(PreReleaseVersion("RC.", 1))
      )
    }

    "create new pre-release version with major version bump" in {
      val expectedVersion = Version(major = 2, minor = 0, patch = 17, preReleaseConfig = preReleaseConfig)
      expectedVersion.newPreRelease(VersionComponent.MAJOR) shouldBe expectedVersion.copy(
        major = 3,
        minor = 0,
        patch = 0,
        preRelease = Some(PreReleaseVersion("RC.", 1))
      )
    }

    "bump pre-release version" in {
      val expectedVersion = Version(
        major = 2,
        minor = 3,
        patch = 11,
        preRelease = Some(PreReleaseVersion("RC.", 1)),
        preReleaseConfig = preReleaseConfig
      )
      expectedVersion.bumpPreRelease shouldBe expectedVersion.copy(preRelease = Some(PreReleaseVersion("RC.", 2)))
    }

  }
}
