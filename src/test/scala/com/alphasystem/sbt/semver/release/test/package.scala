package com.alphasystem
package sbt
package semver
package release

import com.alphasystem.sbt.semver.release.common.{JGitAdapter, TestRepository}
import com.alphasystem.sbt.semver.release.internal.{SemanticBuildVersion, SemanticBuildVersionConfiguration, Version}
import io.circe.{Decoder, Encoder, Json}
import org.scalatest.prop.TableDrivenPropertyChecks.*
import org.scalatest.prop.TableFor1
import sbtsemverrelease.{PreReleaseConfig, VersionsMatching}

import scala.util.matching.Regex
package object test {

  private[test] val majorVersionMatchingRegex: Regex = "(?<=major: )[0-9]*".r
  private[test] val minorVersionMatchingRegex: Regex = "(?<=minor: )[0-9]*".r
  private[test] val patchVersionMatchingRegex: Regex = "(?<=patch: )[0-9]*".r

  val AnnotatedTestData: TableFor1[Boolean] = Table("annotated", false, true)

  implicit val encodeRegEx: Encoder[Regex] = (a: Regex) => Json.fromString(a.regex)

  implicit val decodeRegex: Decoder[Regex] =
    Decoder.decodeString.map(value => new Regex(value))

  implicit val encodeVersionComponent: Encoder[VersionComponent] =
    (a: VersionComponent) => Json.fromString(a.name())

  implicit val decodeVersionComponent: Decoder[VersionComponent] =
    Decoder.decodeString.map(value => VersionComponent.valueOf(value))

  private[test] def toTagNames(src: String): List[String] =
    if (src == "[]") Nil
    else src.dropRight(1).drop(1).split(",").map(_.trim).toList

  implicit class TestRepositoryOps(src: TestRepository) {
    def createTag(semanticBuildVersion: SemanticBuildVersion, annotated: Boolean = false): TestRepository =
      src.tag(semanticBuildVersion.determineVersion, annotated)

    def createTag(version: String): TestRepository = src.tag(version, annotated = true)
  }

  implicit class JGitAdapterOps(src: JGitAdapter) {
    def getCurrentHeadTag: String =
      src
        .getTagsForCurrentBranch
        .map(_.replaceAll(DefaultTagPrefix, ""))
        .map(version => Version(version, DefaultSnapshotSuffix, PreReleaseConfig()))
        .min
        .toStringValue()
  }
}
