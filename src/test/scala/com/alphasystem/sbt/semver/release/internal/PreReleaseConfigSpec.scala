package com.alphasystem.sbt.semver.release.internal

import org.scalatest.funsuite.AnyFunSuite

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
      intercept[IllegalArgumentException](PreReleaseConfig("1.0.0-alpha.01"))
    assert(
      caught.getMessage === "Numeric identifiers must not include leading zeroes"
    )
  }
}
