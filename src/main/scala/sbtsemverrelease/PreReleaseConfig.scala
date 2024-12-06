package sbtsemverrelease

import com.alphasystem.sbt.semver.release.DefaultPreReleasePattern

import scala.util.matching.Regex

case class PreReleaseConfig(
  startingVersion: String = "RC.1",
  preReleasePartPattern: String = DefaultPreReleasePattern) {

  lazy val preReleasePartPatternRegEx: Regex = preReleasePartPattern.r

  lazy val pattern: Regex = ("\\d++\\.\\d++\\.\\d++-" + preReleasePartPattern).r
}
