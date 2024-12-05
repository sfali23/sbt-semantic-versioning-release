package com.alphasystem
package sbt
package semver
package release
package internal

import com.alphasystem.sbt.semver.release.{DefaultSnapshotSuffix, DefaultTagPrefix}
import com.alphasystem.sbt.semver.release.common.{JGitAdapter, TestRepository}
import sbtsemverrelease.PreReleaseConfig

package object scenarios {

  implicit class TestRepositoryOps(src: TestRepository) {
    def createTag(version: String): TestRepository = src.tag(version, annotated = true)
  }

  implicit class JGitAdapterOps(src: JGitAdapter) {
    def getCurrentHeadTag: String =
      src
        .getTagsForCurrentBranch
        .map(_.replaceAll(DefaultTagPrefix, ""))
        .map(version => Version(version, DefaultSnapshotSuffix, PreReleaseConfig()))
        .min
        .toStringValue()
  }
}
