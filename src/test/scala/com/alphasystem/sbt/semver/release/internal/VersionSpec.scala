package com.alphasystem
package sbt
package semver
package release
package internal

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sbtsemverrelease.PreReleaseConfig

class VersionSpec extends AnyWordSpec with Matchers {

  import VersionComponent.*

  private val preReleaseConfig = PreReleaseConfig()

  "Version parser" should {

    "parse simple version" in {
      Version("0.12.345", DefaultSnapshotPrefix, PreReleaseConfig()) shouldBe Version(0, 12, 345)
    }

    "parse hot fix version" in {
      Version("1.89.22.1", DefaultSnapshotPrefix, PreReleaseConfig()) shouldBe Version(1, 89, 22, Some(1))
    }

    "parse pre-release version" in {
      Version("1.89.22-RC.5", DefaultSnapshotPrefix, preReleaseConfig) shouldBe
        Version(
          1,
          89,
          22,
          preRelease = Some(PreReleaseVersion("RC.", 5)),
          preReleaseConfig = preReleaseConfig
        )
    }

    "parse snapshot version without meta info" in {
      Version("2.123.7-SNAPSHOT", DefaultSnapshotPrefix, preReleaseConfig) shouldBe
        Version(2, 123, 7, snapshot = Some(Snapshot(DefaultSnapshotPrefix)), preReleaseConfig = preReleaseConfig)
    }

    "parse snapshot version wit meta info" in {
      Version("0.45.799-SNAPSHOT+abcde", DefaultSnapshotPrefix, preReleaseConfig) shouldBe
        Version(
          0,
          45,
          799,
          snapshot = Some(Snapshot(DefaultSnapshotPrefix, Some("abcde"))),
          preReleaseConfig = preReleaseConfig
        )
    }

    "parse version containing both pre-release followed by snapshot" in {
      Version("6.0.13-RC.3-SNAPSHOT+abcd", DefaultSnapshotPrefix, preReleaseConfig) shouldBe
        Version(
          6,
          0,
          13,
          None,
          Some(PreReleaseVersion("RC.", 3)),
          Some(Snapshot(DefaultSnapshotPrefix, Some("abcd"))),
          preReleaseConfig
        )
    }
  }

  "Version sort" should {
    "compare simple version" in {
      val actual = Seq(
        Version("0.12.346", DefaultSnapshotPrefix, preReleaseConfig),
        Version("0.12.345-RC.2", DefaultSnapshotPrefix, preReleaseConfig),
        Version("0.12.346.1", DefaultSnapshotPrefix, preReleaseConfig),
        Version("1.89.22-RC.5", DefaultSnapshotPrefix, preReleaseConfig),
        Version("0.12.345", DefaultSnapshotPrefix, preReleaseConfig),
        Version("0.12.345-RC.1", DefaultSnapshotPrefix, preReleaseConfig)
      ).sorted.map(_.toStringValue)
      val expected = Seq("1.89.22-RC.5", "0.12.346.1", "0.12.346", "0.12.345", "0.12.345-RC.2", "0.12.345-RC.1")
      actual shouldBe expected
    }

    "sort versions" in {
      val actual = Seq(
        Version("0.1.0", DefaultSnapshotPrefix, preReleaseConfig),
        Version("0.2.0", DefaultSnapshotPrefix, preReleaseConfig),
        Version("0.2.1", DefaultSnapshotPrefix, preReleaseConfig),
        Version("1.0.0", DefaultSnapshotPrefix, preReleaseConfig),
        Version("1.1.0", DefaultSnapshotPrefix, preReleaseConfig),
        Version("1.1.0-RC.1", DefaultSnapshotPrefix, preReleaseConfig),
        Version("1.1.0-RC.2", DefaultSnapshotPrefix, preReleaseConfig)
      ).sorted.map(_.toStringValue)
      val expected = Seq("1.1.0", "1.1.0-RC.2", "1.1.0-RC.1", "1.0.0", "0.2.1", "0.2.0", "0.1.0")
      actual shouldBe expected
    }
  }

  "Version" should {

    "bump patch version" in {
      val expectedVersion = Version(major = 0, minor = 1, patch = 0, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpVersion(None, PATCH) shouldBe expectedVersion.copy(patch = 1)
    }

    "bump minor version" in {
      val expectedVersion = Version(major = 0, minor = 1, patch = 3, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpVersion(None, MINOR) shouldBe expectedVersion.copy(minor = 2, patch = 0)
    }

    "bump major version" in {
      val expectedVersion = Version(major = 0, minor = 5, patch = 23, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpVersion(None, MAJOR) shouldBe expectedVersion.copy(major = 1, minor = 0, patch = 0)
    }

    "bump hotfix version" in {
      val expectedVersion = Version(major = 1, minor = 2, patch = 7, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpVersion(None, HOT_FIX) shouldBe expectedVersion.copy(hotfix = Some(1))
    }

    "create new pre-release version with patch version bump" in {
      val expectedVersion = Version(major = 2, minor = 0, patch = 17, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpVersion(None, PATCH, NEW_PRE_RELEASE) shouldBe expectedVersion.copy(
        patch = 18,
        preRelease = Some(PreReleaseVersion("RC.", 1))
      )
    }

    "create new pre-release version with minor version bump" in {
      val expectedVersion = Version(major = 2, minor = 0, patch = 17, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpVersion(None, MINOR, NEW_PRE_RELEASE) shouldBe expectedVersion.copy(
        minor = 1,
        patch = 0,
        preRelease = Some(PreReleaseVersion("RC.", 1))
      )
    }

    "create new pre-release version with major version bump" in {
      val expectedVersion = Version(major = 2, minor = 0, patch = 17, preReleaseConfig = preReleaseConfig)
      expectedVersion.bumpVersion(None, MAJOR, NEW_PRE_RELEASE) shouldBe expectedVersion.copy(
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
      expectedVersion.bumpVersion(None, PRE_RELEASE) shouldBe
        expectedVersion.copy(preRelease = Some(PreReleaseVersion("RC.", 2)))
    }

    "create snapshot version with minor version bump" in {
      val expectedVersion = Version(major = 1, minor = 3, patch = 5, preReleaseConfig = preReleaseConfig)
      val snapshot = Some(Snapshot("SNAPSHOT", Some("asdqwe")))
      expectedVersion.bumpVersion(snapshot, SNAPSHOT, MINOR) shouldBe
        expectedVersion.copy(minor = 4, patch = 0, snapshot = snapshot)
    }
  }
}
