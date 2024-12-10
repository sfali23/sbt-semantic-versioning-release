package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.*
import sbtsemverrelease.{AutoBump, PreReleaseConfig}

import scala.util.matching.Regex

case class SemanticBuildVersionConfiguration(
  startingVersion: String = DefaultStartingVersion,
  tagPrefix: String = DefaultTagPrefix,
  snapshotSuffix: String = DefaultSnapshotSuffix,
  forceBump: Boolean = DefaultForceBump,
  promoteToRelease: Boolean = DefaultPromoteToRelease,
  snapshot: Boolean = DefaultSnapshot,
  newPreRelease: Boolean = DefaultNewPreRelease,
  autoBump: AutoBump = AutoBump(),
  defaultBumpLevel: VersionComponent = DefaultBumpLevel,
  componentToBump: VersionComponent = DefaultComponentToBump,
  preReleaseConfig: PreReleaseConfig = PreReleaseConfig(),
  hotfixBranchPattern: Regex = DefaultHotfixBranchPattern,
  extraReleaseBranches: Seq[String] = Seq.empty) {

  private lazy val releaseBranches: Set[String] = (extraReleaseBranches ++ DefaultReleaseBranches).toSet

  def isAutoBumpEnabled: Boolean = autoBump.isEnabled

  def isReleaseBranch(branchName: String): Boolean = releaseBranches.contains(branchName)
}
