package com.alphasystem
package sbt
package semver
package release

import release.common.{JGitAdapter, TestRepository}
import release.internal.{SemanticBuildVersion, SemanticBuildVersionConfiguration, Version}
import sbtsemverrelease.AutoBump.*
import sbtsemverrelease.{AutoBump, PreReleaseConfig, SnapshotConfig}
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try
import scala.util.matching.Regex

package object test {

  implicit class TestRepositoryOps(src: TestRepository) {
    def createTag(semanticBuildVersion: SemanticBuildVersion, annotated: Boolean = false): TestRepository =
      src.tag(semanticBuildVersion.determineVersion, annotated)

    def createTag(version: String): TestRepository = src.tag(version, annotated = true)
  }

  implicit class JGitAdapterOps(src: JGitAdapter) {
    def getCurrentHeadTag(
      tagPrefix: String = DefaultTagPrefix,
      snapshotSuffix: String = DefaultSnapshotPrefix,
      preReleaseConfig: PreReleaseConfig = PreReleaseConfig()
    ): String =
      src
        .getTagsForCurrentBranch
        .map(_.replaceAll(tagPrefix, ""))
        .flatMap(version => Try(Version(version, snapshotSuffix, preReleaseConfig)).toOption)
        .min
        .toStringValue
  }

  implicit class ConfigOps(src: Config) {
    def readFailSafeString(path: String, defaultValue: String): String =
      if (src.hasPath(path)) src.getString(path) else defaultValue

    def readFailSafeInt(path: String, defaultValue: Int): Int =
      if (src.hasPath(path)) src.getInt(path) else defaultValue

    def readFailSafeBoolean(path: String, defaultValue: Boolean): Boolean =
      if (src.hasPath(path)) src.getBoolean(path) else defaultValue

    def readFailSafeRegex(path: String, defaultValue: Regex): Regex =
      if (src.hasPath(path)) src.getString(path).r else defaultValue

    def toSemanticBuildVersionConfiguration: SemanticBuildVersionConfiguration =
      SemanticBuildVersionConfiguration(
        startingVersion = src.readFailSafeString("startingVersion", DefaultStartingVersion),
        tagPrefix = src.readFailSafeString("tagPrefix", DefaultTagPrefix),
        forceBump = src.readFailSafeBoolean("forceBump", DefaultBooleanValue),
        promoteToRelease = src.readFailSafeBoolean("promoteToRelease", DefaultBooleanValue),
        snapshot = src.readFailSafeBoolean("snapshot", DefaultBooleanValue),
        newPreRelease = src.readFailSafeBoolean("newPreRelease", DefaultBooleanValue),
        autoBump = if (src.hasPath("autoBump")) src.getConfig("autoBump").toAutoBump else AutoBump(),
        defaultBumpLevel =
          VersionComponent.valueOf(src.readFailSafeString("defaultBumpLevel", DefaultBumpLevel.name())),
        componentToBump =
          VersionComponent.valueOf(src.readFailSafeString("componentToBump", DefaultComponentToBump.name())),
        snapshotConfig =
          if (src.hasPath("snapshotConfig")) src.getConfig("snapshotConfig").toSnapshotConfig else SnapshotConfig(),
        preReleaseConfig =
          if (src.hasPath("preReleaseConfig")) src.getConfig("preReleaseConfig").toPreReleaseConfig
          else PreReleaseConfig(),
        hotfixBranchPattern = src.readFailSafeRegex("hotfixBranchPattern", DefaultHotfixBranchPattern),
        extraReleaseBranches = Seq.empty
      )

    def toAutoBump: AutoBump =
      AutoBump(
        majorPattern = Some(src.readFailSafeRegex("majorPattern", DefaultMajorPattern)),
        minorPattern = Some(src.readFailSafeRegex("minorPattern", DefaultMinorPattern)),
        patchPattern = Some(src.readFailSafeRegex("patchPattern", DefaultPatchPattern)),
        newPreReleasePattern = Some(src.readFailSafeRegex("newPreReleasePattern", DefaultNewPreReleasePattern)),
        promoteToReleasePattern = Some(src.readFailSafeRegex("promoteToReleasePattern", DefaultPromoteToReleasePattern))
      )

    def toPreReleaseConfig: PreReleaseConfig =
      PreReleaseConfig(
        prefix = src.readFailSafeString("prefix", "RC"),
        separator = src.readFailSafeString("separator", "."),
        startingVersion = src.readFailSafeInt("startingVersion", 1)
      )

    def toSnapshotConfig: SnapshotConfig =
      SnapshotConfig(
        prefix = src.readFailSafeString("prefix", DefaultSnapshotPrefix),
        appendCommitHash = src.readFailSafeBoolean("appendCommitHash", defaultValue = true),
        useShortHash = src.readFailSafeBoolean("useShortHash", defaultValue = true)
      )
  }

  def toSemanticBuildVersionConfiguration(src: String): SemanticBuildVersionConfiguration =
    ConfigFactory.parseString(src).toSemanticBuildVersionConfiguration

  def toSemanticBuildVersionConfiguration(
    resourceName: String,
    configPaths: Seq[String]
  ): SemanticBuildVersionConfiguration = {
    val fullConfig = ConfigFactory.load(resourceName)
    val config =
      configPaths.foldLeft(ConfigFactory.empty()) { case (config, path) =>
        fullConfig.getConfig(path).withFallback(config)
      }
    config.withFallback(fullConfig.getConfig("default")).toSemanticBuildVersionConfiguration
  }
}
