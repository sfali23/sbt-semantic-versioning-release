package com.alphasystem
package sbt
package semver

import sbtsemverrelease.PreReleaseConfig

import scala.util.matching.Regex

package object release {

  val DefaultStartingVersion: String = "0.1.0"
  val DefaultTagPrefix: String = "v"
  val DefaultTagPattern: Regex = "\\d++\\.\\d++\\.\\d++".r
  val DefaultSnapshotSuffix: String = "SNAPSHOT"
  val DefaultForceBump: Boolean = false
  val DefaultPromoteToRelease: Boolean = false
  val DefaultSnapshot: Boolean = true
  val DefaultNewPreRelease: Boolean = false
  val DefaultBumpLevel: VersionComponent = VersionComponent.PATCH
  val DefaultComponentToBump: VersionComponent = VersionComponent.NONE
  val DefaultHotfixBranchPattern: Regex = s"^$DefaultTagPrefix(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\+$$".r
  val DefaultReleaseBranches: Seq[String] = Seq("main", "master")

  private val SystemPropertyNamePrefix = "sbt.release."

  val StartingVersionSystemPropertyName =
    s"${SystemPropertyNamePrefix}startingVersion"

  val ForceBumpSystemPropertyName = s"${SystemPropertyNamePrefix}forceBump"

  val NewPreReleaseSystemPropertyName =
    s"${SystemPropertyNamePrefix}newPreRelease"

  val PromoteToReleaseSystemPropertyName =
    s"${SystemPropertyNamePrefix}promoteToRelease"

  val SnapshotSystemPropertyName = s"${SystemPropertyNamePrefix}snapshot"

  val ComponentToBumpSystemPropertyName =
    s"${SystemPropertyNamePrefix}componentToBump"

  implicit class StringOps(src: String) {
    def replaceNewLines: String = src.replaceAll(System.lineSeparator(), "")
  }

  implicit class VersionComponentOps(src: VersionComponent) {
    def <(other: VersionComponent): Boolean = src.ordinal() < other.ordinal()
  }

  implicit class RegexOps(src: Regex) {
    def isEmpty(source: String): Boolean = src.findFirstIn(source).isEmpty

    def nonEmpty(source: String): Boolean = src.findFirstIn(source).nonEmpty
  }

}
