package sbtsemverrelease

import scala.util.matching.Regex

case class PreReleaseConfig(
  startingVersion: String = "RC.1",
  preReleasePartPattern: String = "^(RC)(.)([1-9]\\d*)$") {

  lazy val preReleasePartPatternRegEx: Regex = preReleasePartPattern.r

  lazy val pattern: Regex = ("\\d++\\.\\d++\\.\\d++-" + preReleasePartPattern).r
}
