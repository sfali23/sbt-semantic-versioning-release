package com.alphasystem.sbt.semver.release.plugin

import com.alphasystem.sbt.semver.release._
import com.alphasystem.sbt.semver.release.internal._
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport.{
  ReleaseStep,
  releaseProcess,
  releaseVersion
}

import scala.util.matching.Regex

object SemanticVersioningReleasePlugin extends AutoPlugin {

  object autoImport {

    val determineVersion = taskKey[String]("releaseVersion")

    val startingVersion = settingKey[String]("startingVersion")

    val tagPrefix = settingKey[String](
      "This option defines an optional prefix to use when tagging a release. Default value is \"v\"."
    )
    val tagPattern = settingKey[Regex]("tagPattern")
    val snapshotSuffix = settingKey[String]("snapshotSuffix")
    val preReleasePrefix = settingKey[String]("preReleasePrefix")
    val autoBumpEnable = settingKey[Boolean]("autoBumpEnable")
    val forceBump = settingKey[Boolean]("forceBump")
    val newPreRelease = settingKey[Boolean]("newPreRelease")
    val promoteToRelease = settingKey[Boolean]("promoteToRelease")
    val snapshot = settingKey[Boolean]("snapshot")
    val componentToBump = settingKey[Option[String]]("componentToBump")
    val autoBumpMajorPattern = settingKey[Option[Regex]]("autoBumpMajorPattern")
    val autoBumpMinorPattern = settingKey[Option[Regex]]("autoBumpMinorPattern")
    val autoBumpPatchPattern = settingKey[Option[Regex]]("autoBumpPatchPattern")

    val autoBumpNewPreReleasePattern =
      settingKey[Option[Regex]]("autoBumpNewPreReleasePattern")

    val autoBumpPromoteToReleasePattern =
      settingKey[Option[Regex]]("autoBumpPromoteToReleasePattern")
    val majorVersionsMatching = settingKey[Int]("majorVersionsMatching")
    val minorVersionsMatching = settingKey[Int]("minorVersionsMatching")
    val patchVersionsMatching = settingKey[Int]("patchVersionsMatching")
    val pushTag = settingKey[Boolean]("pushTag")

    object ReleaseKeys {
      val versionToBe = AttributeKey[String]("Next version")
    }
  }

  import autoImport._

  override def trigger = allRequirements

  override def requires: Plugins = ReleasePlugin

  private def toConfiguration =
    Def.task {
      SemanticBuildVersionConfiguration(
        startingVersion = startingVersion.value,
        tagPrefix = tagPrefix.value,
        tagPattern = tagPattern.value,
        snapshotSuffix = snapshotSuffix.value,
        preReleasePrefix = preReleasePrefix.value,
        autoBumpEnable = autoBumpEnable.value,
        forceBump = forceBump.value,
        promoteToRelease = promoteToRelease.value,
        snapshot = snapshot.value,
        newPreRelease = newPreRelease.value,
        autoBump = AutoBump(
          autoBumpMajorPattern.value,
          autoBumpMinorPattern.value,
          autoBumpPatchPattern.value,
          autoBumpNewPreReleasePattern.value,
          autoBumpPromoteToReleasePattern.value
        ),
        versionsMatching = VersionsMatching(
          majorVersionsMatching.value,
          minorVersionsMatching.value,
          patchVersionsMatching.value
        ),
        componentToBump = componentToBump
          .value
          .map(VersionComponent.valueOf)
          .getOrElse(DefaultComponentToBump)
      )
    }

  private def getStartingVersion =
    Def.setting {
      val versionInBuild = (version in ThisBuild).value
      val parts = versionInBuild.split("-").toList
      parts match {
        case Nil => DefaultStartingVersion

        case head :: _ =>
          if (VersioningHelper.isValidStartingVersion(head))
            head
          else DefaultStartingVersion
      }
    }

  override def projectSettings: Seq[Def.Setting[_]] = Seq[Setting[_]](
    startingVersion := getStartingVersion.value,
    tagPrefix := DefaultTagPrefix,
    tagPattern := DefaultTagPattern,
    snapshotSuffix := DefaultSnapshotSuffix,
    preReleasePrefix := DefaultPreReleasePrefix,
    autoBumpEnable := DefaultAutoBumpEnable,
    forceBump := DefaultForceBump,
    promoteToRelease := DefaultPromoteToRelease,
    snapshot := DefaultSnapshot,
    newPreRelease := DefaultNewPreRelease,
    componentToBump := Some(DefaultComponentToBump.name()),
    autoBumpMajorPattern := Some(AutoBump.DefaultMajorPattern),
    autoBumpMinorPattern := Some(AutoBump.DefaultMinorPattern),
    autoBumpPatchPattern := Some(AutoBump.DefaultPatchPattern),
    autoBumpNewPreReleasePattern := Some(AutoBump.DefaultNewPreReleasePattern),
    autoBumpPromoteToReleasePattern := Some(
      AutoBump.DefaultPromoteToReleasePattern
    ),
    majorVersionsMatching := -1,
    minorVersionsMatching := -1,
    patchVersionsMatching := -1,
    pushTag := true,
    determineVersion :=
      SemanticBuildVersion(
        baseDirectory.value,
        toConfiguration.value
      ).determineVersion,
    releaseVersion := { _ =>
      determineVersion.value: @sbtUnchecked
    },
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      tagRelease,
      publishArtifacts
    )
  )
}
