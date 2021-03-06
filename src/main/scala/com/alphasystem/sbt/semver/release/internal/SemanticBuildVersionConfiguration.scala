package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release._
import sbtsemverrelease.{ AutoBump, PreReleaseConfig, VersionsMatching }

import scala.util.matching.Regex

case class SemanticBuildVersionConfiguration(
  startingVersion: String = DefaultStartingVersion,
  tagPrefix: String = DefaultTagPrefix,
  tagPattern: Regex = DefaultTagPattern,
  snapshotSuffix: String = DefaultSnapshotSuffix,
  forceBump: Boolean = DefaultForceBump,
  promoteToRelease: Boolean = DefaultPromoteToRelease,
  snapshot: Boolean = DefaultSnapshot,
  newPreRelease: Boolean = DefaultNewPreRelease,
  autoBump: AutoBump = AutoBump(),
  versionsMatching: VersionsMatching = VersionsMatching(),
  componentToBump: VersionComponent = DefaultComponentToBump,
  preReleaseConfig: PreReleaseConfig = PreReleaseConfig(),
  preReleaseBump: (PreReleaseConfig, String) => String =
    defaultPreReleaseBump) {

  def isAutobumpEnabled: Boolean = autoBump.isEnabled

}
