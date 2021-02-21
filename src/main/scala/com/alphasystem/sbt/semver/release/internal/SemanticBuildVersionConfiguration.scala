package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.VersionComponent

import scala.util.matching.Regex

case class SemanticBuildVersionConfiguration(
  startingVersion: String = "0.1.0",
  tagPrefix: String = "v",
  tagPattern: Regex = "\\d++\\.\\d++\\.\\d++".r,
  snapshotSuffix: String = "SNAPSHOT",
  preReleasePrefix: String = "RC.",
  autoBumpEnable: Boolean = true,
  forceBump: Boolean = false,
  promoteToRelease: Boolean = false,
  snapshot: Boolean = true,
  newPreRelease: Boolean = false,
  autoBump: AutoBump = AutoBump(),
  versionsMatching: VersionsMatching = VersionsMatching(),
  componentToBump: VersionComponent = VersionComponent.NONE) {

  def isAutobumpEnabled: Boolean = autoBumpEnable && autoBump.isEnabled

}
