package com.alphasystem.sbt.semver.release.internal

import scala.util.matching.Regex

case class PreReleaseConfig(
  startingVersion: String = "RC.1",
  preReleasePartPattern: String = ".*+$") {

  validate()

  def pattern: Regex = ("\\d++\\.\\d++\\.\\d++-" + preReleasePartPattern).r

  def splitComponents(latestVersion: String): List[String] =
    latestVersion.split("(?<=\\D)(?=\\d)").toList

  private def validate(): Unit = {
    if (!VersioningHelper.isValidPreReleasePart(s"0.1.0-$startingVersion")) {
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
  }
}
