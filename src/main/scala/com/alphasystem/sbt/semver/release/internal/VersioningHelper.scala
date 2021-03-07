package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release._

import scala.util.matching.Regex

object VersioningHelper {

  val VersionStartRegex: Regex = "^(\\d+\\.\\d+\\.\\d+)".r

  val PreReleasePartRegex: Regex =
    "(?<=^\\d+\\.\\d+\\.\\d+)-(?<preReleasePart>.*)$".r

  private val SnapshotPartRegex: String => Regex =
    (snapshotSuffix: String) =>
      s"(?<=^\\d+\\.\\d+\\.\\d+-)($snapshotSuffix)$$".r

  private val MainVersionPartRegex = "^[^-]*-".r

  /** Determine the next version to bump.
    *
    * @param config        current configuration
    * @param latestVersion latest version from version control
    * @return next [[VersionComponent]]
    */
  private[release] def determineVersionToBump(
    config: SemanticBuildVersionConfiguration,
    latestVersion: String
  ): VersionComponent = {
    if (config.componentToBump.isNone) {
      val validPreReleasePart = !isSnapshotVersion(
        latestVersion,
        config.snapshotSuffix
      ) && isValidPreReleasePart(latestVersion)
      if (config.promoteToRelease) {
        if (validPreReleasePart) {
          // We are promoting a pre-release to release, so nothing to bump
          VersionComponent.NONE
        } else
          throw new IllegalArgumentException(
            s"""Cannot bump version because the latest version is '$latestVersion', which doesn't pre-release
               | identifier. However, promoteToRelease flag was on"""
              .stripMargin
              .replaceNewLines
          )
      } else if (config.newPreRelease) {
        // If latest version is not a pre-release and if we are making a new pre-release, then bump patch
        VersionComponent.PATCH
      } else if (!validPreReleasePart) {
        // If latest version is not a pre-release then bump patch
        VersionComponent.PATCH
      } else VersionComponent.PRE_RELEASE
    } else config.componentToBump
  }

  private[release] def incrementVersion(
    config: SemanticBuildVersionConfiguration,
    latestVersion: String
  ): String = {
    val latest = createSemanticVersion(latestVersion, 4)

    config.componentToBump match {
      case VersionComponent.MAJOR =>
        val label = latest.bumpMajor.toLabel
        if (config.newPreRelease)
          s"$label-${config.preReleaseConfig.startingVersion}"
        else label
      case VersionComponent.MINOR =>
        val label = latest.bumpMinor.toLabel
        if (config.newPreRelease)
          s"$label-${config.preReleaseConfig.startingVersion}"
        else label
      case VersionComponent.PATCH =>
        val label = latest.bumpPatch.toLabel
        if (config.newPreRelease)
          s"$label-${config.preReleaseConfig.startingVersion}"
        else label
      case VersionComponent.PRE_RELEASE =>
        if (!isValidPreReleasePart(latestVersion)) {
          throw new IllegalArgumentException(
            """Cannot bump pre-release because the latest version is not a pre-release version. To create a new 
              |pre-release version, use newPreRelease instead"""
              .stripMargin
              .replaceNewLines
          )
        } else {
          val bumpedPreReleasePart = config.preReleaseBump(
            config.preReleaseConfig,
            MainVersionPartRegex.replaceAllIn(latestVersion, "")
          )
          s"${latest.toLabel}-$bumpedPreReleasePart"
        }
      case VersionComponent.NONE => latest.toLabel
    }
  }

  private[release] def determineIncrementedVersionFromStartingVersion(
    config: SemanticBuildVersionConfiguration
  ): String = {
    var latestVersion = config.startingVersion
    val latest = createSemanticVersion(latestVersion, 3)

    val componentToBump = config.componentToBump
    val bumpMajor =
      componentToBump.isMajor && (latest.major == 0 || latest.minor == 1 || latest.patch == 1)
    val bumpMinor =
      componentToBump.isMinor && ((latest.major == 0 && latest.minor == 0) || latest.patch == 1)
    val bumpPatch =
      (componentToBump.isPatch || componentToBump.isNone) &&
        (latest.major == 0 && latest.minor == 0 && latest.patch == 0)

    if (bumpMajor || bumpMinor) {
      latestVersion = incrementVersion(config, latestVersion)
    } else if (bumpPatch) {
      latestVersion = incrementVersion(
        config.copy(componentToBump =
          VersionComponent.PATCH
        ), // componentToBump could be None
        latestVersion
      )
    } else if (componentToBump.isPreRelease) {
      // VersionComponent.PRE_RELEASE
      // starting version never contains pre-release identifiers
      throw new IllegalArgumentException(
        """Cannot bump pre-release because the latest version is not a pre-release version. To create a new pre-release
          | version, use newPreRelease instead""".stripMargin.replaceNewLines
      )
    }
    if (config.newPreRelease && !isValidPreReleasePart(latestVersion)) {
      latestVersion =
        s"$latestVersion-${config.preReleaseConfig.startingVersion}"
    }
    latestVersion
  }

  def isValidPreReleasePart(version: String): Boolean =
    PreReleasePartRegex.nonEmpty(version)

  private def isSnapshotVersion(
    version: String,
    snapshotSuffix: String
  ): Boolean =
    SnapshotPartRegex(snapshotSuffix).nonEmpty(version)

  def isValidStartingVersion(version: String): Boolean =
    VersionStartRegex.nonEmpty(version)

  private def createSemanticVersion(
    latestVersion: String,
    limit: Int
  ): SemanticVersion = {
    val components = latestVersion.split("[.-]", limit)
    SemanticVersion(
      components(VersionComponent.MAJOR.getIndex).toInt,
      components(VersionComponent.MINOR.getIndex).toInt,
      components(VersionComponent.PATCH.getIndex).toInt
    )
  }
}
