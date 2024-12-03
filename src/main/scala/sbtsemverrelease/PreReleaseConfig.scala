package sbtsemverrelease

import com.alphasystem.sbt.semver.release.internal.VersioningHelper

import scala.util.matching.Regex

case class PreReleaseConfig(
  startingVersion: String = "RC.1",
  preReleasePartPattern: String = "^(RC)(.)([1-9]\\d*)$") {

  validate()

  lazy val preReleasePartPatternRegEx: Regex = preReleasePartPattern.r

  lazy val pattern: Regex = ("\\d++\\.\\d++\\.\\d++-" + preReleasePartPattern).r

  /** Splits the given `preReleasePart` separating into numeric and non-numeric parts.
    *
    * For example: If the input is '''alpha.0''' then result would be '''["alpha", ".", "0"]''' If the input is
    * '''alpha0''' then result would be '''["alpha", "0"]''' If the input is '''pre.1-alpha.1''' then result would be
    * '''["pre", ".", "1", "-", "alpha", ".", "1"]'''
    * @param preReleasePart
    *   pre-release part of the current version
    * @return
    *   List of different parts of pre-release part
    */
  def splitComponents(preReleasePart: String): List[String] =
    preReleasePart
      .split("(?<=[\\D.-])(?=[\\d.-])|(?<=[\\d.-])(?=[\\D.-])")
      .toList

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
