package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release._

import scala.util.matching.Regex

case class SemanticBuildVersionConfiguration(
  startingVersion: String = DefaultStartingVersion,
  tagPrefix: String = DefaultTagPrefix,
  tagPattern: Regex = DefaultTagPattern,
  snapshotSuffix: String = DefaultSnapshotSuffix,
  preReleasePrefix: String = DefaultPreReleasePrefix,
  forceBump: Boolean = DefaultForceBump,
  promoteToRelease: Boolean = DefaultPromoteToRelease,
  snapshot: Boolean = DefaultSnapshot,
  newPreRelease: Boolean = DefaultNewPreRelease,
  autoBump: AutoBump = AutoBump(),
  versionsMatching: VersionsMatching = VersionsMatching(),
  componentToBump: VersionComponent = DefaultComponentToBump) {

  def isAutobumpEnabled: Boolean = autoBump.isEnabled

}
