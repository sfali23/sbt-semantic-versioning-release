package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.*
import sbtsemverrelease.{AutoBump, PreReleaseConfig, SnapshotConfig}

import scala.util.matching.Regex

case class SemanticBuildVersionConfiguration(
  startingVersion: String = DefaultStartingVersion,
  tagPrefix: String = DefaultTagPrefix,
  forceBump: Boolean = DefaultBooleanValue,
  promoteToRelease: Boolean = DefaultBooleanValue,
  snapshot: Boolean = DefaultBooleanValue,
  newPreRelease: Boolean = DefaultBooleanValue,
  autoBump: AutoBump = AutoBump(),
  defaultBumpLevel: VersionComponent = DefaultBumpLevel,
  componentToBump: VersionComponent = DefaultComponentToBump,
  snapshotConfig: SnapshotConfig = SnapshotConfig(),
  preReleaseConfig: PreReleaseConfig = PreReleaseConfig(),
  hotfixBranchPattern: Regex = DefaultHotfixBranchPattern,
  extraReleaseBranches: Seq[String] = Seq.empty) {

  private lazy val releaseBranches: Set[String] = (extraReleaseBranches ++ DefaultReleaseBranches).toSet

  def isAutoBumpEnabled: Boolean = autoBump.isEnabled

  def isReleaseBranch(branchName: String): Boolean = releaseBranches.contains(branchName)
}
