package com.alphasystem.sbt.semver.release.internal

case class SemanticVersion(major: Int, minor: Int, patch: Int) {

  def bumpMajor: SemanticVersion = copy(major = major + 1, minor = 0, patch = 0)

  def bumpMinor: SemanticVersion = copy(minor = minor + 1, patch = 0)

  def bumpPatch: SemanticVersion = copy(patch = patch + 1)

  def toLabel = s"$major.$minor.$patch"
}
