package com.alphasystem.sbt.semver.release

import org.scalatest.funsuite.AnyFunSuite

class PreReleaseConfigSpec extends AnyFunSuite {

  test("Providing valid data should result in providing pattern") {
    val config = PreReleaseConfig("1.0.0-RC.1")
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

  test(
    "Having starting version without prerelease part should result in failed validation"
  ) {
    val startingVersion = "1.0.0"
    val caught =
      intercept[IllegalArgumentException](PreReleaseConfig(startingVersion))
    assert(
      caught.getMessage === s"Starting version ($startingVersion) is not a valid prerelease version"
    )
  }

  test(
    "Having starting version without hyphen in prerelease part should result in failed validation"
  ) {
    val startingVersion = "1.0.0RC.0"
    val caught =
      intercept[IllegalArgumentException](PreReleaseConfig(startingVersion))
    assert(
      caught.getMessage === s"Starting version ($startingVersion) is not a valid prerelease version"
    )
  }

  test(
    "Should result in failure is there is no numeric part in the pre-release part"
  ) {
    val caught =
      intercept[IllegalArgumentException](PreReleaseConfig("1.0.0-RC"))
    assert(
      caught.getMessage === "pre-release must have at least one numeric part"
    )
  }
}
