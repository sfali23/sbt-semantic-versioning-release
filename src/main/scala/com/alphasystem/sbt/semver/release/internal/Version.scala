package com.alphasystem
package sbt
package semver
package release
package internal

import sbtsemverrelease.PreReleaseConfig

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

case class Version(
  major: Int,
  minor: Int,
  patch: Int,
  hotfix: Option[Int] = None,
  preRelease: Option[PreReleaseVersion] = None,
  snapshot: Option[Snapshot] = None,
  preReleaseConfig: PreReleaseConfig = PreReleaseConfig()) {

  import VersionComponent.*

  def isHotfix: Boolean = hotfix.isDefined

  def isPreRelease: Boolean = preRelease.isDefined

  private def bumpMajor: Version = {
    if (preRelease.nonEmpty) {
      throw new IllegalArgumentException("Current version is a a pre-release.")
    }
    copy(major = major + 1, minor = 0, patch = 0, hotfix = None, preRelease = None)
  }

  private def bumpMinor: Version = {
    if (preRelease.nonEmpty) {
      throw new IllegalArgumentException("Current version is a a pre-release.")
    }
    copy(minor = minor + 1, patch = 0)
  }

  private def bumpPatch: Version = {
    if (preRelease.nonEmpty) {
      throw new IllegalArgumentException("Current version is a a pre-release.")
    }
    copy(patch = patch + 1)
  }

  private def bumpHotfix: Version = {
    if (preRelease.nonEmpty) {
      throw new IllegalArgumentException("Current version is a a pre-release.")
    }
    copy(hotfix = hotfix.map(_ + 1).orElse(Some(1)))
  }

  private def bumpPreRelease: Version = {
    if (preRelease.isEmpty) {
      throw new IllegalArgumentException(
        """Cannot bump pre-release because the latest version is not a pre-release version. To create a new pre-release
          | version, use newPreRelease instead""".stripMargin.replaceNewLines
      )
    }
    copy(preRelease = preRelease.map(pr => pr.copy(version = pr.version + 1)))
  }

  private def newPreRelease: Version = {
    if (preRelease.isDefined) {
      throw new IllegalArgumentException("Current version is already pre-release")
    }
    copy(preRelease = preReleaseConfig.toInitialPreReleaseVersion)
  }

  private def promoteToRelease: Version = copy(preRelease = None)

  private def bumpSnapshot(snapshot: Option[Snapshot]): Version = copy(snapshot = snapshot)

  def bumpVersion(snapshot: Option[Snapshot], componentsToBump: VersionComponent*): Version =
    componentsToBump.foldLeft(this) { case (result, versionComponent) =>
      versionComponent match {
        case NONE               => result
        case MAJOR              => result.bumpMajor
        case MINOR              => result.bumpMinor
        case PATCH              => result.bumpPatch
        case HOT_FIX            => result.bumpHotfix
        case NEW_PRE_RELEASE    => result.newPreRelease
        case PRE_RELEASE        => result.bumpPreRelease
        case PROMOTE_TO_RELEASE => result.promoteToRelease
        case SNAPSHOT           => result.bumpSnapshot(snapshot)
      }
    }

  def toStringValue(tagPrefix: String = DefaultTagPrefix): String = {
    val hotfixValue = hotfix.map(v => s".$v").getOrElse("")
    val preReleaseValue = preRelease.map(_.toStringValue).getOrElse("")
    val snapshotValue = snapshot.map(_.toStringValue).getOrElse("")
    s"$tagPrefix$major.$minor.$patch$hotfixValue$preReleaseValue$snapshotValue"
  }
}

object Version {

  private val VersionRegex =
    """^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|
      |\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?(?:\.(0|[1-9]\d*))?$"""
      .stripMargin
      .replaceAll(System.lineSeparator(), "")
      .r

  def apply(
    major: Int,
    minor: Int,
    patch: Int,
    hotfix: Option[Int] = None,
    preRelease: Option[PreReleaseVersion] = None,
    snapshot: Option[Snapshot] = None,
    preReleaseConfig: PreReleaseConfig = PreReleaseConfig()
  ): Version = new Version(
    major = major,
    minor = minor,
    patch = patch,
    hotfix = hotfix,
    preRelease = preRelease,
    snapshot = snapshot,
    preReleaseConfig = preReleaseConfig
  )

  def apply(version: String, snapshotSuffix: String, preReleaseConfig: PreReleaseConfig): Version = {
    val matcher = VersionRegex.findAllIn(version)
    // Group 0 will be the entire version string, Groups 1 - 3 will major, minor, and patch version, so there must be
    // at least four groups. There will total of 6 groups
    Try(matcher.groupCount) match {
      case Failure(_)                  => throw new IllegalArgumentException(s"Invalid version: $version")
      case Success(value) if value < 4 => throw new IllegalArgumentException(s"Invalid version: $version")
      case _                           => // do nothing
    }
    // pre-release and snapshot
    val maybePreReleaseOrSnapshot = Option(matcher.group(4))
    val metaInfo = Option(matcher.group(5))

    val maybePreReleaseVersion =
      maybePreReleaseOrSnapshot.map(_.replaceAll(s"-$snapshotSuffix", "")) match {
        case Some(value) => preReleaseConfig.toPreReleaseVersion(value)
        case None        => None
      }

    val maybeSnapshot = maybePreReleaseOrSnapshot
      .map(_.replaceAll(preReleaseConfig.preReleasePartPattern, ""))
      .filter(_.contains(snapshotSuffix))
      .map(_ => Snapshot(snapshotSuffix, metaInfo))

    Version(
      major = matcher.group(1).toInt,
      minor = matcher.group(2).toInt,
      patch = matcher.group(3).toInt,
      hotfix = Option(matcher.group(6)).map(_.toInt),
      preRelease = maybePreReleaseVersion,
      snapshot = maybeSnapshot,
      preReleaseConfig = preReleaseConfig
    )
  }

  // This is descending ordering
  implicit val VersionOrdering: Ordering[Version] = (v1: Version, v2: Version) => {
    val hotFixValues = (v2.hotfix.getOrElse(0), v1.hotfix.getOrElse(0))
    val preReleaseValues =
      (v2.preRelease.map(_.version).getOrElse(Int.MaxValue), v1.preRelease.map(_.version).getOrElse(Int.MaxValue))
    compareTo(List((v2.major, v1.major), (v2.minor, v1.minor), (v2.patch, v1.patch), hotFixValues, preReleaseValues))
  }

  @tailrec
  private def compareTo(src: List[(Int, Int)]): Int =
    src match {
      case Nil => 0
      case (v1, v2) :: tail =>
        v1.compareTo(v2) match {
          case 0 => compareTo(tail)
          case v => v
        }
    }
}

case class PreReleaseVersion(prefix: String, version: Int, suffix: Option[String] = None) {

  def toStringValue: String = s"-$prefix$version${suffix.getOrElse("")}"
}

object PreReleaseVersion {
  implicit val PreReleaseVersionOrdering: Ordering[PreReleaseVersion] = (x: PreReleaseVersion, y: PreReleaseVersion) =>
    y.version.compareTo(x.version)
}

case class Snapshot(suffix: String, meta: Option[String] = None) {

  def toStringValue: String = s"-$suffix${meta.map(value => s"+$value").getOrElse("")}"
}
