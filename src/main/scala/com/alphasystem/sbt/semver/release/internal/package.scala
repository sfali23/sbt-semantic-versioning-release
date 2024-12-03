package com.alphasystem
package sbt
package semver
package release

import sbtsemverrelease.PreReleaseConfig

import scala.util.Try

package object internal {

  implicit class PreReleaseConfigOps(src: PreReleaseConfig) {
    def toPreReleaseVersion(version: String): Option[PreReleaseVersion] = {
      val matchIterator = src.preReleasePartPatternRegEx.findAllIn(version)
      if (matchIterator.nonEmpty) {
        // skip group 0, since it contains entire matched string
        val v =
          (1 to matchIterator.groupCount).foldLeft(PreReleaseVersion("", -1)) { case (m, groupNumber) =>
            val value = matchIterator.group(groupNumber)
            val maybeVersion = Try(value.toInt).toOption

            if (m.version >= 0) {
              // we already have version, anything after would be the suffix
              m.copy(suffix = m.suffix.map(s => s"$s$value"))
            } else if (m.version < 0 && maybeVersion.isDefined) {
              // this should be the version
              m.copy(version = maybeVersion.get)
            } else {
              // this should be the prefix
              m.copy(prefix = s"${m.prefix}$value")
            }
          }

        Some(v)
      } else None
    }
  }
}
