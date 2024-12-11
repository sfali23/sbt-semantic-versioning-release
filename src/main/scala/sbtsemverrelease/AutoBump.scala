package sbtsemverrelease

import com.alphasystem.sbt.semver.release.*
import sbtsemverrelease.AutoBump.*

import scala.util.matching.Regex

case class AutoBump(
  majorPattern: Option[Regex] = Some(DefaultMajorPattern),
  minorPattern: Option[Regex] = Some(DefaultMinorPattern),
  patchPattern: Option[Regex] = Some(DefaultPatchPattern),
  newPreReleasePattern: Option[Regex] = Some(DefaultNewPreReleasePattern),
  promoteToReleasePattern: Option[Regex] = Some(DefaultPromoteToReleasePattern)) {

  def major(input: String): Boolean = matchValue(input, majorPattern)

  def minor(input: String): Boolean = matchValue(input, minorPattern)

  def patch(input: String): Boolean = matchValue(input, patchPattern)

  def newPreRelease(input: String): Boolean =
    matchValue(input, newPreReleasePattern)

  def promoteToRelease(input: String): Boolean =
    matchValue(input, promoteToReleasePattern)

  private def matchValue(input: String, regex: Option[Regex]): Boolean = {
    // git command line put commit message withing quotation park and if regex has '^', then matching doesn't work
    // remove starting "
    val result =
      Option(input)
        .map { r =>
          if (input.startsWith("\"")) r.replaceFirst("\"", "") else r
        }
        .getOrElse("")
    regex.exists(_.nonEmpty(result))
  }

  def isEnabled: Boolean =
    majorPattern.nonEmpty ||
      minorPattern.nonEmpty ||
      patchPattern.nonEmpty ||
      newPreReleasePattern.nonEmpty ||
      promoteToReleasePattern.nonEmpty
}

object AutoBump {

  val DefaultMajorPattern: Regex = "\\[major]".r
  val DefaultMinorPattern: Regex = "\\[minor]".r
  val DefaultPatchPattern: Regex = "\\[patch]".r
  val DefaultNewPreReleasePattern: Regex = "\\[new-pre-release]".r
  val DefaultPromoteToReleasePattern: Regex = "\\[promote]".r

}
