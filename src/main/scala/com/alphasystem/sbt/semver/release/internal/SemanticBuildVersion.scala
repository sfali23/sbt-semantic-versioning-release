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
  private val adapter: JGitAdapter = JGitAdapter(workingDir)
  private val preReleaseConfig = baseConfig.preReleaseConfig
  private val snapshotConfig = baseConfig.snapshotConfig
  private val snapshotSuffix = snapshotConfig.prefix
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

  def getUnReleasedCommits(version: String): List[String] = adapter.getUnReleasedCommits(s"$tagPrefix$version")

  def determineVersion: String = {
    val currentBranch = adapter.getCurrentBranch
    val hotfixRequired = baseConfig.hotfixBranchPattern.nonEmpty(currentBranch)
    val snapshotRequired = {
      val notAReleaseBranch = !baseConfig.isReleaseBranch(currentBranch)
      val hasUncommitedChanges = adapter.hasUncommittedChanges
      val snapshotFlag = baseConfig.snapshot
      if (notAReleaseBranch) {
        logger.warn(
          "Current configuration doesn't allow to create new tag from current branch ({}), creating snapshot version",
          currentBranch
        )
      }
      if (hasUncommitedChanges) {
        logger.warn("Current branch ({}) has uncommitted changes, creating snapshot version", currentBranch)
      }
      if (snapshotFlag) {
        logger.warn("Snapshot flag is set to true, creating snapshot version")
      }
      (notAReleaseBranch || hasUncommitedChanges || snapshotFlag) && !hotfixRequired
    }

    val maybeLatestVersion = latestVersion
    val currentVersion = maybeLatestVersion.getOrElse(startingVersion)
    val newVersion = determineVersion(currentVersion, hotfixRequired, snapshotRequired, maybeLatestVersion)
    if (currentVersion == newVersion && maybeLatestVersion.isDefined) {
      throw new IllegalArgumentException(
        s"Couldn't determine next version, tag (${newVersion.toStringValue}) is already exists."
      )
    }
    newVersion.toStringValue
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
          .addComponentIfRequired(
            baseConfig.componentToBump,
            addDefaultComponent(baseConfig.componentToBump)
          )
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
            val commits = adapter.getCommitBetween(s"$tagPrefix${version.toStringValue}")
            (commits.nonEmpty, commits)
          }
          .getOrElse((false, adapter.getCommits))

      val versionComponents =
        commitMessages.foldLeft(SetupVersionComponentsForBump()) { case (m, commitMessage) =>
          m.parseMessage(commitMessage, baseConfig.autoBump)
        }

      // if we have auto bump enabled, there is(are) previous tag(s), and if there are commits added between last tag
      // and current head, then if components to bump are empty at the end of process, then bump default configured bump level
      val forcePush = baseConfig.isAutoBumpEnabled && maybeLatestVersion.isDefined && hasCommits
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

    if (versionComponents.hasPromoteToRelease) {
      if (currentVersion.isPreRelease) {
        // when current version is pre-release and promote to release is set, ignore any other flags
        logger.warn("Promoting to release flag is set and current version is pre-release, ignoring any other flag.")
        versionComponents.reset().addPromoteToRelease()
      } else {
        logger.warn("Promoting to release flag is set but current version is not a pre-release version, ignoring it.")
        versionComponents.removePromoteToRelease()
      }
    }

    if (versionComponents.hasMajor) {
      versionComponents.removeMinor().removePatch()
    }

    if (versionComponents.hasMinor) {
      versionComponents.removePatch()
    }

    if (snapshotRequired) {
      // This is special case, we figured this is snapshot version, but we don't know which component to bump, bump defaultBumpLevel
      if (
        !versionComponents.hasMandatoryComponents && !versionComponents.hasPreRelease && !versionComponents.hasPromoteToRelease
      )
        versionComponents
          .addComponentIfRequired(baseConfig.defaultBumpLevel, addDefaultComponent(baseConfig.defaultBumpLevel))

      versionComponents.addSnapshot()
    }

    if (!versionComponents.hasEssentialComponents) {
      if (forcePush) {
        // We don't have any defined bump level use defaultBumpLevel
        versionComponents.addComponentIfRequired(baseConfig.defaultBumpLevel, () => forcePush)
      } else if (baseConfig.forceBump) {
        throw new IllegalArgumentException(
          s"Couldn't determine next version, tag (${currentVersion.toStringValue}) is already exists."
        )
      }
    }

    currentVersion.bumpVersion(getSnapshotInfo, versionComponents.getVersionComponents*)
  }

  private def getSnapshotInfo = {
    val hash =
      if (snapshotConfig.appendCommitHash) {
        if (snapshotConfig.useShortHash) Try(adapter.getShortHash).toOption
        else Try(adapter.getHeadCommit.getName).toOption
      } else None
    Some(Snapshot(snapshotSuffix, hash))
  }

  private def addDefaultComponent(versionComponent: VersionComponent) = () =>
    Seq(VersionComponent.MAJOR, VersionComponent.MINOR, VersionComponent.PATCH)
      .contains(versionComponent)

}

object SemanticBuildVersion {

  def apply(
    workingDir: File,
    baseConfig: SemanticBuildVersionConfiguration = SemanticBuildVersionConfiguration()
  ): SemanticBuildVersion = new SemanticBuildVersion(workingDir, baseConfig)
}
