package com.alphasystem.sbt.semver.release.internal

import com.alphasystem.sbt.semver.release.*

import scala.util.matching.Regex

object VersioningHelper {

  private val VersionStartRegex: Regex = "^(\\d+\\.\\d+\\.\\d+)".r

  def isValidStartingVersion(version: String): Boolean =
    VersionStartRegex.nonEmpty(version)

}
