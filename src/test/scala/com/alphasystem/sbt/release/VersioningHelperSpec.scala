package com.alphasystem.sbt.release

import org.scalatest.funspec.AnyFunSpec

class VersioningHelperSpec extends AnyFunSpec {
  describe("determineVersionToBump") {
    it("should return the version to bump provided") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.MINOR
      )
      assert(
        VersioningHelper.determineVersionToBump(
          config,
          "0.1.1"
        ) === VersionComponent.MINOR
      )
    }

    it(
      "should return `NONE` if `promoteToRelease` flag is on, with valid version"
    ) {
      val config = SemanticBuildVersionConfiguration(
        promoteToRelease = true
      )
      assert(
        VersioningHelper.determineVersionToBump(
          config,
          "0.1.1-RC1"
        ) === VersionComponent.NONE
      )
    }

    it(
      "should result in failure if `promoteToRelease` flag is on, with invalid version"
    ) {
      val config = SemanticBuildVersionConfiguration(
        promoteToRelease = true
      )
      val latestVersion = "0.1.1"
      val caught =
        intercept[IllegalArgumentException](
          VersioningHelper.determineVersionToBump(config, latestVersion)
        )
      assert(
        caught.getMessage === s"""Cannot bump version because the latest version is '$latestVersion', which
                                 | doesn't pre-release identifier. However, promoteToRelease flag was on"""
          .stripMargin
          .replaceNewLines
      )
    }

    it(
      "should bump `PATCH` latestVersion is not pre-release"
    ) {
      val config = SemanticBuildVersionConfiguration()
      assert(
        VersioningHelper.determineVersionToBump(
          config,
          "0.1.1"
        ) === VersionComponent.PATCH
      )
    }

    it(
      "should bump `PATCH` if `newPreRelease` flag is on and latestVersion is not pre-release"
    ) {
      val config = SemanticBuildVersionConfiguration(
        newPreRelease = true
      )
      assert(
        VersioningHelper.determineVersionToBump(
          config,
          "0.1.1"
        ) === VersionComponent.PATCH
      )
    }

    /* it(
      "should result in failure if latestVersion is pre-release and `newPreRelease` flag is on"
    ) {
      val config =
        SemanticBuildVersionConfiguration(
          newPreRelease = true
        )
      val latestVersion = "0.1.1-RC1"
      val caught =
        intercept[IllegalArgumentException](
          VersioningHelper.determineVersionToBump(config, latestVersion)
        )
      assert(
        caught.getMessage === s"""Cannot bump version because the latest version is '$latestVersion',
                                 |which is already a pre-release version"""
          .stripMargin
          .replaceNewLines
      )
    }*/

    it("should bump pre-release version") {
      val startingVersion = "0.1.1-RC.0"
      val config = SemanticBuildVersionConfiguration()
      assert(
        VersioningHelper.determineVersionToBump(
          config,
          startingVersion
        ) === VersionComponent.PRE_RELEASE
      )
    }

  } // end of "determineVersionToBump"

  describe("incrementVersion") {
    it("should bump major version") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.MAJOR
      )
      assert(VersioningHelper.incrementVersion(config, "2.0.0") === "3.0.0")
    }

    it("should bump major version with newPreRelease") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.MAJOR,
        newPreRelease = true
      )
      assert(
        VersioningHelper.incrementVersion(config, "1.1.0") === "2.0.0-RC.1"
      )
    }

    it("should bump minor version") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.MINOR
      )
      assert(VersioningHelper.incrementVersion(config, "1.2.0") === "1.3.0")
    }

    it("should bump minor version with newPreRelease") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.MINOR,
        newPreRelease = true
      )
      assert(
        VersioningHelper.incrementVersion(config, "1.1.0") === "1.2.0-RC.1"
      )
    }

    it("should bump patch version without newPreRelease") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.PATCH
      )
      assert(VersioningHelper.incrementVersion(config, "1.2.0") === "1.2.1")
    }

    it("should bump patch version with newPreRelease") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.PATCH,
        newPreRelease = true
      )
      assert(
        VersioningHelper.incrementVersion(config, "1.2.1") === "1.2.2-RC.1"
      )
    }

    it("should bump pre-release version with `dot` as separator") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.PRE_RELEASE
      )
      assert(
        VersioningHelper.incrementVersion(
          config,
          "0.4.3-alpha.0"
        ) === "0.4.3-alpha.1"
      )
    }

    it("should bump pre-release version without separator") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.PRE_RELEASE
      )
      assert(
        VersioningHelper.incrementVersion(
          config,
          "0.4.3-alpha1"
        ) === "0.4.3-alpha2"
      )
    }

    it("should not bump version when no component to bump") {
      val config = SemanticBuildVersionConfiguration(
      )
      assert(VersioningHelper.incrementVersion(config, "1.2.0") === "1.2.0")
    }

    it(
      "should result in failure when bumping pre-release without pre-release part"
    ) {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.PRE_RELEASE
      )
      val caught =
        intercept[IllegalArgumentException](
          VersioningHelper.incrementVersion(config, "1.2.0")
        )
      assert(
        caught.getMessage === """Cannot bump pre-release because the latest version is not a pre-release version. 
                                |To create a new pre-release version, use newPreRelease instead"""
          .stripMargin
          .replaceNewLines
      )
    }
  } // end of "incrementVersion"

  describe("determineIncrementedVersionFromStartingVersion") {
    it("should not bump starting version") {
      val config = SemanticBuildVersionConfiguration()
      assert(
        VersioningHelper.determineIncrementedVersionFromStartingVersion(
          config
        ) === config.startingVersion
      )
    }

    it("should bump major version") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.MAJOR
      )
      assert(
        VersioningHelper.determineIncrementedVersionFromStartingVersion(
          config
        ) === "1.0.0"
      )
    }

    it("should bump minor version") {
      val config = SemanticBuildVersionConfiguration(
        startingVersion = "0.0.1",
        componentToBump = VersionComponent.MINOR
      )
      assert(
        VersioningHelper.determineIncrementedVersionFromStartingVersion(
          config
        ) === "0.1.0"
      )
    }

    it("should bump patch version") {
      val config = SemanticBuildVersionConfiguration(
        startingVersion = "0.0.0",
        componentToBump = VersionComponent.PATCH
      )
      assert(
        VersioningHelper.determineIncrementedVersionFromStartingVersion(
          config
        ) === "0.0.1"
      )
    }

    it("should attach pre-release suffix is newPreRelease flag is on") {
      val config = SemanticBuildVersionConfiguration(
        newPreRelease = true
      )
      assert(
        VersioningHelper.determineIncrementedVersionFromStartingVersion(
          config
        ) === "0.1.0-RC.1"
      )
    }

    it("should result in failure if bump component is pre-release") {
      val config = SemanticBuildVersionConfiguration(
        componentToBump = VersionComponent.PRE_RELEASE
      )
      val caught =
        intercept[IllegalArgumentException](
          VersioningHelper.determineIncrementedVersionFromStartingVersion(
            config
          )
        )
      assert(
        caught.getMessage === """Cannot bump pre-release because the latest version is not a pre-release version. To
                                | create a new pre-release version, use newPreRelease instead"""
          .stripMargin
          .replaceNewLines
      )
    }
  } // end of "determineIncrementedVersionFromStartingVersion"
}
