package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release._

import scala.util.matching.Regex

object VersioningHelper {

  val VersionStartRegex: Regex = "^(\\d+\\.\\d+\\.\\d+)".r

  val PreReleasePartRegex: Regex =
    "(?<=^\\d+\\.\\d+\\.\\d+)-(?<preReleasePart>.*)$".r

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
      val validPreReleasePart = isValidPreReleasePart(latestVersion)
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
        // TODO: enable if we are going to re-enable pre-release config
        /*if (validPreReleasePart) {
          throw new IllegalArgumentException(
            s"""Cannot bump version because the latest version is '$latestVersion', which is already a pre-release
               |version""".stripMargin.replaceNewLines
          )
        } else*/
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
          s"$label-${config.preReleasePrefix}1"
        else label
      case VersionComponent.MINOR =>
        val label = latest.bumpMinor.toLabel
        if (config.newPreRelease)
          s"$label-${config.preReleasePrefix}1"
        else label
      case VersionComponent.PATCH =>
        val label = latest.bumpPatch.toLabel
        if (config.newPreRelease)
          s"$label-${config.preReleasePrefix}1"
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
          s"${latest.toLabel}-${bumpPreReleaseVersion(latestVersion)}"
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
      latestVersion = s"$latestVersion-${config.preReleasePrefix}1"
    }
    latestVersion
  }

  def isValidPreReleasePart(version: String): Boolean =
    PreReleasePartRegex.findFirstIn(version).nonEmpty

  def isValidStartingVersion(version: String): Boolean =
    VersionStartRegex.findFirstIn(version).nonEmpty

  private def bumpPreReleaseVersion(latestVersion: String) = {
    val preReleasePart = "^[^-]*-".r.replaceAllIn(latestVersion, "")
    val preReleaseComponents = preReleasePart.split("(?<=\\D)(?=\\d)")
    val preReleasePrefix = preReleaseComponents.dropRight(1).mkString("")
    s"$preReleasePrefix${preReleaseComponents.last.toInt + 1}"
  }

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
