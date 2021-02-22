package com.alphasystem.sbt.semver

import scala.util.matching.Regex

package object release {

  val DefaultStartingVersion: String = "0.1.0"
  val DefaultTagPrefix: String = "v"
  val DefaultTagPattern: Regex = "\\d++\\.\\d++\\.\\d++".r
  val DefaultSnapshotSuffix: String = "SNAPSHOT"
  val DefaultPreReleasePrefix: String = "RC."
  val DefaultAutoBumpEnable: Boolean = true
  val DefaultForceBump: Boolean = false
  val DefaultPromoteToRelease: Boolean = false
  val DefaultSnapshot: Boolean = true
  val DefaultNewPreRelease: Boolean = false
  val DefaultComponentToBump: VersionComponent = VersionComponent.NONE

  implicit class StringOps(src: String) {
    def replaceNewLines: String = src.replaceAll(System.lineSeparator(), "")
  }

  implicit class VersionComponentOps(src: VersionComponent) {
    def <(other: VersionComponent): Boolean = src.ordinal() < other.ordinal()
  }

}
