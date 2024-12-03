package com.alphasystem
package sbt
package semver
package release
package internal

import sbtsemverrelease.PreReleaseConfig

import scala.annotation.tailrec

case class Version(
  major: Int,
  minor: Int,
  patch: Int,
  hotfix: Option[Int] = None,
  preRelease: Option[PreReleaseVersion] = None,
  snapshot: Option[Boolean] = None)

object Version {

  private val VersionRegex =
    """^v(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|
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
    snapshot: Option[Boolean] = None
  ): Version = new Version(
    major = major,
    minor = minor,
    patch = patch,
    hotfix = hotfix,
    preRelease = preRelease,
    snapshot = snapshot
  )

  def apply(version: String, preReleaseConfig: PreReleaseConfig): Version = {
    val matcher = VersionRegex.findAllIn(version)
    // Group 0 will be the entire version string, Groups 1 - 3 will major, minor, and patch version, so there must be
    // at least four groups. There will total of 6 groups
    if (matcher.groupCount < 4) {
      throw new IllegalArgumentException(s"Invalid version: $version")
    }
    // pre-release and snapshot
    val maybePreReleaseOrSnapshot = Option(matcher.group(4))

    val maybePreReleaseVersion =
      maybePreReleaseOrSnapshot match {
        case Some(value) => preReleaseConfig.toPreReleaseVersion(value)
        case None        => None
      }

    // group 5 contains something line "+build.123", which we are not supporting

    Version(
      major = matcher.group(1).toInt,
      minor = matcher.group(2).toInt,
      patch = matcher.group(3).toInt,
      hotfix = Option(matcher.group(6)).map(_.toInt),
      preRelease = maybePreReleaseVersion,
      snapshot = maybePreReleaseOrSnapshot.filter(_.contains("SNAPSHOT")).map(_.toBoolean)
    )
  }

  implicit val VersionOrdering: Ordering[Version] = (v1: Version, v2: Version) => {
    val hotFixValues = (v1.hotfix.getOrElse(0), v2.hotfix.getOrElse(0))
    val preReleaseValues = (v1.preRelease.map(_.version).getOrElse(0), v2.preRelease.map(_.version).getOrElse(0))
    compareTo(List((v1.major, v2.major), (v1.minor, v2.minor), (v1.patch, v2.patch), hotFixValues, preReleaseValues))
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

case class PreReleaseVersion(prefix: String, version: Int, suffix: Option[String] = None)

object PreReleaseVersion {
  implicit val PreReleaseVersionOrdering: Ordering[PreReleaseVersion] = (x: PreReleaseVersion, y: PreReleaseVersion) =>
    x.version.compareTo(y.version)
}
