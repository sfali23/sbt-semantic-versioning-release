package com.alphasystem
package sbt
package semver

import scala.util.matching.Regex

package object release {

  val DefaultStartingVersion: String = "0.1.0"
  val DefaultTagPrefix: String = "v"
  val DefaultSnapshotSuffix: String = "SNAPSHOT"
  val DefaultForceBump: Boolean = false
  val DefaultPromoteToRelease: Boolean = false
  val DefaultSnapshot: Boolean = false
  val DefaultNewPreRelease: Boolean = false
  val DefaultBumpLevel: VersionComponent = VersionComponent.PATCH
  val DefaultComponentToBump: VersionComponent = VersionComponent.NONE
  val DefaultHotfixBranchPattern: Regex = initializeHotfixBranchPattern()
  val DefaultReleaseBranches: Seq[String] = Seq("main", "master")
  val DefaultPreReleasePattern: String = "^(RC)(.)([1-9]\\d*)$"

  private val SystemPropertyNamePrefix = "sbt.release."

  val ForceBumpSystemPropertyName = s"${SystemPropertyNamePrefix}forceBump"

  val NewPreReleaseSystemPropertyName = s"${SystemPropertyNamePrefix}newPreRelease"

  val PromoteToReleaseSystemPropertyName = s"${SystemPropertyNamePrefix}promoteToRelease"

  val SnapshotSystemPropertyName = s"${SystemPropertyNamePrefix}snapshot"

  val DefaultBumpLevelSystemPropertyName = s"${SystemPropertyNamePrefix}defaultBumpLevel"

  val ComponentToBumpSystemPropertyName = s"${SystemPropertyNamePrefix}componentToBump"

  val HotfixBranchPatternSystemPropertyName = s"${SystemPropertyNamePrefix}hotFixBranchPattern"

  def initializeHotfixBranchPattern(tagPrefix: String = DefaultTagPrefix): Regex =
    s"^$tagPrefix(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\+$$".r

  implicit class StringOps(src: String) {
    def replaceNewLines: String = src.replaceAll(System.lineSeparator(), "")
  }

  implicit class RegexOps(src: Regex) {
    def isEmpty(source: String): Boolean = src.findFirstIn(source).isEmpty

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
