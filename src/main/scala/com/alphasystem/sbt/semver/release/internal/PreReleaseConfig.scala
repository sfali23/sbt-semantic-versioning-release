package com.alphasystem.sbt.semver.release.internal

import scala.util.Try
import scala.util.matching.Regex

case class PreReleaseConfig(
  startingVersion: String,
  preReleasePartPattern: String = ".*+$") {

  private var preReleasePrefix = ""
  private var preReleaseVersion = 0

  validate()

  def pattern: Regex = ("\\d++\\.\\d++\\.\\d++-" + preReleasePartPattern).r

  def bump: String = s"$preReleasePrefix${preReleaseVersion + 1}"

  private def validate(): Unit = {
    if (!VersioningHelper.isValidPreReleasePart(startingVersion)) {
      throw new IllegalArgumentException(
        s"Starting version ($startingVersion) is not a valid prerelease version"
      )
    }
    validateComponents()
  }

  private def validateComponents(): Unit = {
    val components = startingVersion.split("\\.")
    components.foreach { component =>
      if ("[\\p{Alnum}-]++".r.findAllIn(component).isEmpty) {
        throw new IllegalArgumentException(
          "Identifiers must comprise only ASCII alphanumerics and hyphen"
        )
      }
      if ("0\\d+".r.findAllIn(startingVersion).nonEmpty) {
        throw new IllegalArgumentException(
          "Numeric identifiers must not include leading zeroes"
        )
      }
    }

    val preReleasePart = "^[^-]*-".r.replaceAllIn(startingVersion, "")
    val preReleaseComponents = preReleasePart.split("(?<=\\D)(?=\\d)")
    if (Try(preReleaseComponents.last.toInt).toOption.isEmpty) {
      // last part of pre-release must be numeric
      throw new IllegalArgumentException(
        "pre-release must have at least one numeric part"
      )
    }

    preReleasePrefix = preReleaseComponents.dropRight(1).mkString("")
    preReleaseVersion = preReleaseComponents.last.toInt
  }
}
