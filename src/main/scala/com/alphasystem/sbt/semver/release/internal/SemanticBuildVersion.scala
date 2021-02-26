package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.common.JGitAdapter
import com.alphasystem.sbt.semver.release.{ internal, _ }
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.RevCommit

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

class SemanticBuildVersion(
  workingDir: File,
  config: SemanticBuildVersionConfiguration) {

  import SemanticBuildVersion._

  private val adapter = JGitAdapter(workingDir)
  private val tags = adapter.getTags
  private var filteredTags: Set[String] = Set.empty
  private var versionByTag: Map[String, String] = Map.empty
  private var autobumpMessages: List[String] = List.empty
  private var maybeLatestVersion: Option[String] = None
  private var _currentConfig = config

  initState()

  def latestVersion: Option[String] = maybeLatestVersion

  def currentConfig: SemanticBuildVersionConfiguration = _currentConfig

  def determineVersion: String = {
    _currentConfig = SetupVersionComponentUsingAutobumpConfiguration(
      _currentConfig,
      autobumpMessages
    )

    if (_currentConfig.newPreRelease && _currentConfig.promoteToRelease) {
      throw new IllegalArgumentException(
        "Creating a new pre-release while also promoting a pre-release is not supported"
      )
    }

    if (
      _currentConfig.promoteToRelease && !_currentConfig.componentToBump.isNone
    ) {
      throw new IllegalArgumentException(
        "Bumping any component while also promoting a pre-release is not supported"
      )
    }

    if (
      _currentConfig.newPreRelease &&
      _currentConfig.componentToBump.isPreRelease
    ) {
      throw new IllegalArgumentException(
        "Bumping pre-release component while also creating a new pre-release is not supported"
      )
    }

    if (!_currentConfig.snapshot && adapter.hasUncommittedChanges) {
      throw new IllegalArgumentException(
        "Cannot create a release version when there are uncommitted changes"
      )
    }

    /*if (newPreRelease && preReleaseConfig.isEmpty) {
      throw new IllegalArgumentException(
        "Cannot create a new pre-release version if a preRelease configuration is not specified"
      )
    }
    if (componentToBump.isPreRelease && preReleaseConfig.isEmpty) {
      throw new IllegalArgumentException(
        "Cannot bump pre-release identifier if a preRelease configuration is not specified"
      )
    }*/

    var result = ""
    if (versionByTag.isEmpty) {
      // This means that we didn't find any tags (taking into account filtering as well) and so we have to use
      // the starting version.

      // We cannot always increment the starting version here. We have to make sure that doing so does not end
      // up skipping a version series or a point-release. The method we are calling here will ensure that we
      // bump only if it is necessary
      result = VersioningHelper
        .determineIncrementedVersionFromStartingVersion(_currentConfig)
    } else {
      val headTag = getLatestTagOnReference(Constants.HEAD)
      if (headTag.isEmpty || adapter.hasUncommittedChanges) {
        _currentConfig = _currentConfig.copy(componentToBump =
          VersioningHelper.determineVersionToBump(
            _currentConfig,
            latestVersion.get
          )
        )
        result =
          VersioningHelper.incrementVersion(_currentConfig, latestVersion.get)
      } else {
        if (
          !_currentConfig.componentToBump.isNone ||
          _currentConfig.newPreRelease ||
          _currentConfig.promoteToRelease
        ) {
          throw new IllegalArgumentException(
            """Cannot bump the version, create a new pre-release version, or promote a pre-release version because HEAD
              | is currently pointing to a tag that identifies an existing version. To be able to create a new version,
              |  you must make changes""".stripMargin.replaceNewLines
          )
        }
        _currentConfig = _currentConfig.copy(snapshot = false)
        result = TagPrefixPattern.replaceAllIn(headTag.get, "")
      }
    }

    if (_currentConfig.snapshot) {
      result = s"$result-${_currentConfig.snapshotSuffix}"
    } else if (versionByTag.contains(result)) {
      throw new IllegalArgumentException(
        s"""Determined version $result already exists on another commit in the repository at '${adapter.directory}'. 
           |Check your configuration to ensure that you haven't forgotten to filter out certain tags or versions. You
           | may also be bumping the wrong component; if so, bump the component that will give you the intended version,
           | or manually create a tag with the intended version on the commit to be released."""
          .stripMargin
          .replaceNewLines
      )
    } else if (filterTags(Set(s"${_currentConfig.tagPrefix}$result")).isEmpty) {
      throw new IllegalArgumentException(
        s"""Determined tag '${_currentConfig.tagPrefix}$result' is filtered out by your configuration; this is not 
           |supported. Check your filtering and tag-prefix configuration. You may also be bumping the wrong component; 
           |if so, bump the component that will give you the intended version, or manually create a tag with the 
           |intended version on the commit to be released."""
          .stripMargin
          .replaceNewLines
      )
    }
    result
  }

  private def initState(): Unit = {
    if (filteredTags.isEmpty) {
      filteredTags = filterTags(tags.keySet)

      versionByTag = filteredTags
        .map(tag => tag -> TagPrefixPattern.replaceFirstIn(tag, ""))
        .toMap

      val headCommit = adapter.getHeadCommit
      if (Option(headCommit).isEmpty) {
        return // If there is no HEAD, we are done
      }

      val nearestAncestorTags: ListBuffer[String] = ListBuffer()
      val references = mutable.Stack[RevCommit]()
      val investigatedReferences = mutable.Set[RevCommit]()
      val autobumpMessages = ListBuffer[String]()

      val revWalk = adapter.getRevWalk

      // This is a depth-first traversal; references is the frontier set (stack)
      references.push(revWalk.parseCommit(headCommit))

      while (references.nonEmpty) {
        val reference = references.pop()
        investigatedReferences += reference
        val tag = getLatestTagOnReference(reference.name())
        if (tag.isDefined && tags.contains(tag.get)) {
          nearestAncestorTags += tag.get
        } else {
          val commit = revWalk.parseCommit(reference.getId)
          if (_currentConfig.isAutobumpEnabled) {
            autobumpMessages += commit.getFullMessage
          }
          references.pushAll(commit.getParents.collect {
            case c if !investigatedReferences.contains(c) => c
          })
        }
      }
      this.autobumpMessages = autobumpMessages.toList

      maybeLatestVersion = nearestAncestorTags
        .toSet
        .map(versionByTag.get)
        .find(_.isDefined)
        .flatten
    }
  }

  private def getLatestTagOnReference(revstr: String): Option[String] = {
    val repository = adapter.repository
    val commit = repository.resolve(revstr)

    tags
      .collect {
        case (name, ref) if filteredTags.contains(name) =>
          name -> repository.resolve(s"${ref.getName}^{commit}")
      }
      .collect {
        case (name, id) if id.equals(commit) => name
      }
      .toList
      .sorted
      .reverse
      .headOption

  }

  // TODO: missing pre-release
  private def filterTags(tags: Set[String]): Set[String] = {
    tags
      .filter(tag => _currentConfig.tagPattern.nonEmpty(tag))
      .filter(tag => SemanticBuildVersion.VersionPattern.nonEmpty(tag))
      .filter(tag =>
        !_currentConfig.versionsMatching.isEnabled || _currentConfig
          .versionsMatching
          .toPattern
          .nonEmpty(tag)
      )
    /*.filter { tag =>
        _currentConfig.preReleaseConfig.isEmpty ||
        PreReleaseRegex.findAllIn(tag).nonEmpty ||
        _currentConfig.preReleaseConfig.get.pattern.findAllIn(tag).nonEmpty
      }*/
  }

}

object SemanticBuildVersion {

  val PreReleaseRegex: Regex = "\\d++\\.\\d++\\.\\d++-[\\p{Alnum}.-]++".r

  val VersionPattern: Regex = ".*\\d++\\.\\d++\\.\\d++.*".r

  val TagPrefixPattern: Regex =
    "^(?!\\d++\\.\\d++\\.\\d)(?:.(?!\\d++\\.\\d++\\.\\d))*.".r

  def apply(
    workingDir: File,
    config: SemanticBuildVersionConfiguration =
      internal.SemanticBuildVersionConfiguration()
  ) = new SemanticBuildVersion(workingDir, config)
}
