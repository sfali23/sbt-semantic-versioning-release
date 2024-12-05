package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.*
import sbtsemverrelease.{AutoBump, PreReleaseConfig, VersionsMatching}

import scala.util.matching.Regex

case class SemanticBuildVersionConfiguration(
  startingVersion: String = DefaultStartingVersion,
  overrideStartingVersion: Option[String] = None, // TODO: remove this, no longer required, needed investigation
  tagPrefix: String = DefaultTagPrefix,
  tagPattern: Regex = DefaultTagPattern, // TODO: remove this, no longer required
  snapshotSuffix: String = DefaultSnapshotSuffix,
  forceBump: Boolean = DefaultForceBump,
  promoteToRelease: Boolean = DefaultPromoteToRelease,
  snapshot: Boolean = DefaultSnapshot,
  newPreRelease: Boolean = DefaultNewPreRelease,
  autoBump: AutoBump = AutoBump(),
  versionsMatching: VersionsMatching = VersionsMatching(), // TODO: remove this, no longer required
  defaultBumpLevel: VersionComponent = DefaultBumpLevel,
  componentToBump: VersionComponent = DefaultComponentToBump,
  preReleaseConfig: PreReleaseConfig = PreReleaseConfig(),
  preReleaseBump: (PreReleaseConfig, String) => String = defaultPreReleaseBump, // TODO: remove this, no longer required
  hotfixBranchPattern: Regex = DefaultHotfixBranchPattern,
  extraReleaseBranches: Seq[String] = Seq.empty) {

  private lazy val releaseBranches: Set[String] = (extraReleaseBranches ++ DefaultReleaseBranches).toSet

  def isAutobumpEnabled: Boolean = autoBump.isEnabled

  def isReleaseBranch(branchName: String): Boolean = releaseBranches.contains(branchName)

  def isHotFixBranch(branchName: String): Boolean = hotfixBranchPattern.nonEmpty(branchName)
}
