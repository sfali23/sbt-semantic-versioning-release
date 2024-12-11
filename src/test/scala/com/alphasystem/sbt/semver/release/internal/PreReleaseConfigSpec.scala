package com.alphasystem
package sbt
package semver
package release
package internal

import org.scalatest.funsuite.AnyFunSuite
import sbtsemverrelease.PreReleaseConfig

class PreReleaseConfigSpec extends AnyFunSuite {

  test("Providing valid data should result in providing pattern") {
    val config = PreReleaseConfig("RC.1")
    assert(
      config
        .pattern
        .pattern
        .pattern() === ("\\d++\\.\\d++\\.\\d++-" + config.preReleasePartPattern)
    )
  }

  test(
    "Having leading zeros in prerelease part should result in failed validation"
  ) {
    val caught =
      intercept[IllegalArgumentException](Version("1.0.0-RC.01", DefaultSnapshotPrefix, PreReleaseConfig()))
    assert(
      caught.getMessage === "Invalid version: 1.0.0-RC.01"
    )
  }
}
