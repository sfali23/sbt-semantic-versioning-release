package sbtsemverrelease

import scala.util.matching.Regex

case class PreReleaseConfig(
  prefix: String = "RC",
  separator: String = ".",
  startingVersion: Int = 1) {

  require(Option(prefix).isDefined && !prefix.isBlank, "prefix cannot be null or empty string")
  require(Option(separator).isDefined && !separator.isBlank, "separator cannot be null or empty string")
  require(startingVersion > 0, "startingVersion must be positive integer greater than 0")

  lazy val preReleasePartPattern: Regex = s"^($prefix)(.)([1-9]\\d*)$$".r

}
