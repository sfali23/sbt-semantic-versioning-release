package com.alphasystem.sbt.semver.release

import com.alphasystem.sbt.semver.release.internal.VersionsMatching
import io.circe.{ Decoder, Encoder, Json }
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor1

import scala.util.matching.Regex
package object test {

  private[test] val majorVersionMatchingRegex: Regex = "(?<=major: )[0-9]*".r
  private[test] val minorVersionMatchingRegex: Regex = "(?<=minor: )[0-9]*".r
  private[test] val patchVersionMatchingRegex: Regex = "(?<=patch: )[0-9]*".r

  val AnnotatedTestData: TableFor1[Boolean] = Table("annotated", false, true)

  implicit val encodeRegEx: Encoder[Regex] = (a: Regex) =>
    Json.fromString(a.regex)

  implicit val decodeRegex: Decoder[Regex] =
    Decoder.decodeString.map(value => new Regex(value))

  implicit val encodeVersionComponent: Encoder[VersionComponent] =
    (a: VersionComponent) => Json.fromString(a.name())

  implicit val decodeVersionComponent: Decoder[VersionComponent] =
    Decoder.decodeString.map(value => VersionComponent.valueOf(value))

  private[test] def toVersionMatching(src: String): VersionsMatching = {
    val major =
      majorVersionMatchingRegex.findFirstIn(src).map(_.toInt).getOrElse(-1)
    val minor =
      minorVersionMatchingRegex.findFirstIn(src).map(_.toInt).getOrElse(-1)
    val patch =
      patchVersionMatchingRegex.findFirstIn(src).map(_.toInt).getOrElse(-1)
    VersionsMatching(major, minor, patch)
  }

  private[test] def toTagNames(src: String): List[String] =
    if (src == "[]") Nil
    else src.dropRight(1).drop(1).split(",").map(_.trim).toList

}
