package com.alphasystem
package sbt
package semver
package release
package internal

import release.common.JGitAdapter
import org.slf4j.LoggerFactory

import java.io.File
import scala.util.Try

class SemanticBuildVersion(workingDir: File, baseConfig: SemanticBuildVersionConfiguration) {

  import VersionComponent.*

  private val logger = LoggerFactory.getLogger(classOf[SemanticBuildVersion])
  private val adapter = JGitAdapter(workingDir)
  private val preReleaseConfig = baseConfig.preReleaseConfig
  private val snapshotSuffix = baseConfig.snapshotSuffix
  private val tagPrefix = baseConfig.tagPrefix
  private val startingVersion = Version(baseConfig.startingVersion, snapshotSuffix, preReleaseConfig)

  def latestVersion: Option[Version] = {
    val tagsForCurrentBranch = adapter
      .getTagsForCurrentBranch
      .map(tag => tag.replaceAll(tagPrefix, ""))
      .flatMap(version => Try(Version(version, snapshotSuffix, preReleaseConfig)).toOption)
      .sorted

    tagsForCurrentBranch.headOption
  }

  def determineVersion: Version = {
    val currentBranch = adapter.getCurrentBranch
    val hotfixRequired = baseConfig.hotfixBranchPattern.nonEmpty(currentBranch)
    val snapshotRequired =
      (!baseConfig.isReleaseBranch(
        currentBranch
      ) || adapter.hasUncommittedChanges) && !hotfixRequired && baseConfig.snapshot

    val maybeLatestVersion = latestVersion
    val currentVersion = maybeLatestVersion.getOrElse(startingVersion)
    val newVersion = determineVersion(currentVersion, hotfixRequired, snapshotRequired, maybeLatestVersion)
    if (currentVersion == newVersion && maybeLatestVersion.isDefined) {
      throw new IllegalArgumentException(
        s"Couldn't determine next version, tag (${newVersion.toStringValue(tagPrefix)}) is already exists."
      )
    }
    newVersion
  }

  private[internal] def determineVersion(
    currentVersion: Version,
    hotfixRequired: Boolean,
    snapshotRequired: Boolean,
    maybeLatestVersion: Option[Version]
  ) =
    if (maybeLatestVersion.isEmpty) {
      // first ever tag on this repository
      startingVersion
    } else if (baseConfig.forceBump) {
      val versionComponents =
        SetupVersionComponentsForBump()
          .addComponentIfRequired(baseConfig.componentToBump, () => true)
          .addComponentIfRequired(PROMOTE_TO_RELEASE, () => baseConfig.promoteToRelease)
          .addComponentIfRequired(NEW_PRE_RELEASE, () => baseConfig.newPreRelease)

      bumpVersion(
        forcePush = false,
        currentVersion = currentVersion,
        hotfixRequired = hotfixRequired,
        snapshotRequired = snapshotRequired,
        versionComponents = versionComponents
      )
    } else {
      val (hasCommits, commitMessages) =
        maybeLatestVersion
          .map { version =>
            // if last tag exists then get commits between last tag and current head, otherwise get all commits
            val commits = adapter.getCommitBetween(version.toStringValue(tagPrefix))
            (commits.nonEmpty, commits)
          }
          .getOrElse((false, adapter.getCommits))

      val versionComponents =
        commitMessages.foldLeft(SetupVersionComponentsForBump()) { case (m, commitMessage) =>
          m.parseMessage(commitMessage, baseConfig.autoBump)
        }

      // if we have auto bump enabled, there is(are) previous tag(s), and if there are commits added between last tag
      // and current head, then if components to bump are empty at the end of process, then bump configured bump level
      val forcePush = baseConfig.isAutobumpEnabled && maybeLatestVersion.isDefined && hasCommits
      bumpVersion(forcePush, currentVersion, hotfixRequired, snapshotRequired, versionComponents)
    }

  private def bumpVersion(
    forcePush: Boolean,
    currentVersion: Version,
    hotfixRequired: Boolean,
    snapshotRequired: Boolean,
    versionComponents: SetupVersionComponentsForBump
  ): Version = {
    if (hotfixRequired || currentVersion.isHotfix) {
      versionComponents
        .addHotFix()
        .removeMajor()
        .removeMinor()
        .removePatch()
        .removePreRelease()
        .removePromoteToRelease()
      if (currentVersion.isHotfix) versionComponents.removeNewPreRelease()
    }

    if (currentVersion.isPreRelease) {
      versionComponents
        .addPreRelease()
        .removeHotFix()
        .removeNewPreRelease()
        .removeMajor()
        .removeMinor()
        .removePatch()
    }

    if (versionComponents.isPromoteToRelease) {
      if (currentVersion.isPreRelease) {
        // when current version is pre-release and promote to release is set, ignore any other flags
        logger.warn("Promoting to release flag is set and current version is pre-release, ignoring any other flag.")
        versionComponents.reset().addPromoteToRelease()
      } else {
        logger.warn("Promoting to release flag is set but current version is not a pre-release version, ignoring it.")
        versionComponents.removePromoteToRelease()
      }
    }

    if (versionComponents.isMajor) {
      versionComponents.removeMinor().removePatch()
    }

    if (versionComponents.isMinor) {
      versionComponents.removePatch()
    }

    if (snapshotRequired) {
      versionComponents.addSnapshot().removeNewPreRelease().removePromoteToRelease().removePreRelease()
    }

    currentVersion.bumpVersion(
      getSnapshotInfo,
      versionComponents
        .addComponentIfRequired(baseConfig.defaultBumpLevel, () => forcePush && versionComponents.isEmpty)
        .getVersionComponents*
    )
  }

  private def getSnapshotInfo = Some(Snapshot(baseConfig.snapshotSuffix, Try(adapter.getShortHash).toOption))
}

object SemanticBuildVersion {

  def apply(
    workingDir: File,
    baseConfig: SemanticBuildVersionConfiguration = SemanticBuildVersionConfiguration()
  ): SemanticBuildVersion =
    new SemanticBuildVersion(workingDir, baseConfig)
}
