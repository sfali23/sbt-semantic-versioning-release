package com.alphasystem.sbt.semver.release

import scala.util.matching.Regex

case class VersionsMatching(major: Int = -1, minor: Int = -1, patch: Int = -1) {
  def isEnabled: Boolean = major >= 0 || minor >= 0 || patch >= 0

  /*
   * Just for display purpose in test
   */
  def toLabel: String = {
    val sb = new StringBuilder("[")
    if (major >= 0) {
      sb.append(s"major: $major")
    }
    if (minor >= 0) {
      if (major >= 0) {
        sb.append(", ")
      }
      sb.append(s"minor: $minor")
    }
    if (patch >= 0) {
      if (minor >= 0) {
        sb.append(", ")
      }
      sb.append(s"patch: $patch")
    }
    sb.append("]")
    sb.toString()
  }

  def toPattern: Regex = {
    if ((major < 0 && minor >= 0) || (minor < 0 && patch >= 0)) {
      throw new IllegalArgumentException(
        """"When specifying a matching versioning-component, all preceding components (if any) must also be specified""""
      )
    }
    var pattern = s"(?<!\\d)$major\\."

    if (minor >= 0) {
      pattern = s"$pattern$minor\\."
      if (patch >= 0) {
        pattern = s"$pattern$patch(?:$$|-)"
      }
    } else {
      pattern = s"$pattern\\d+\\."
    }

    new Regex(pattern)
  }
}
