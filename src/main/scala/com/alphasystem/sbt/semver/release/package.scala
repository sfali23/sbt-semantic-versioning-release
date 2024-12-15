package com.alphasystem
package sbt
package semver

import scala.util.matching.Regex

package object release {

  val DefaultBooleanValue = false
  val DefaultStartingVersion: String = "0.1.0"
  val DefaultTagPrefix: String = "v"
  val DefaultSnapshotPrefix: String = "SNAPSHOT"
  val DefaultBumpLevel: VersionComponent = VersionComponent.PATCH
  val DefaultComponentToBump: VersionComponent = VersionComponent.NONE
  val DefaultHotfixBranchPattern: Regex = initializeHotfixBranchPattern()
  val DefaultReleaseBranches: Seq[String] = Seq("main", "master")

  private val VersionStartRegex: Regex = "^(\\d+\\.\\d+\\.\\d+)".r

  def isValidStartingVersion(version: String): Boolean =
    VersionStartRegex.nonEmpty(version)

  private val SystemPropertyNamePrefix = "sbt.release."

  val ForceBumpSystemPropertyName = s"${SystemPropertyNamePrefix}forceBump"

  val NewPreReleaseSystemPropertyName = s"${SystemPropertyNamePrefix}newPreRelease"

  val PromoteToReleaseSystemPropertyName = s"${SystemPropertyNamePrefix}promoteToRelease"

  val SnapshotSystemPropertyName = s"${SystemPropertyNamePrefix}snapshot"

  val DefaultBumpLevelSystemPropertyName = s"${SystemPropertyNamePrefix}defaultBumpLevel"

  val ComponentToBumpSystemPropertyName = s"${SystemPropertyNamePrefix}componentToBump"

  val HotfixBranchPatternSystemPropertyName = s"${SystemPropertyNamePrefix}hotFixBranchPattern"

  val AddUnReleasedCommitsToTagSummarySystemPropertyName =
    s"${SystemPropertyNamePrefix}addUnReleasedCommitsToTagSummary"

  def initializeHotfixBranchPattern(tagPrefix: String = DefaultTagPrefix): Regex =
    s"^$tagPrefix(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\+$$".r

  implicit class StringOps(src: String) {
    def replaceNewLines: String = src.replaceAll(System.lineSeparator(), "")
  }

  implicit class RegexOps(src: Regex) {
    def nonEmpty(source: String): Boolean = src.findFirstIn(source).nonEmpty
  }

  implicit class ComponentToBumpOps(src: ComponentToBump) {
    def toVersionComponent: VersionComponent =
      src match {
        case ComponentToBump.MAJOR => VersionComponent.MAJOR
        case ComponentToBump.MINOR => VersionComponent.MINOR
        case ComponentToBump.PATCH => VersionComponent.PATCH
      }
  }

}
