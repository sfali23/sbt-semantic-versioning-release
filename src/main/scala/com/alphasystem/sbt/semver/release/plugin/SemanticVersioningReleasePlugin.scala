package com.alphasystem.sbt.semver.release.plugin

import com.alphasystem.sbt.semver.release._
import com.alphasystem.sbt.semver.release.internal._
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._

import java.io.File
import scala.util.matching.Regex

object SemanticVersioningReleasePlugin extends AutoPlugin {

  object autoImport {

    val determineVersion = taskKey[String](
      "A task to determine the next version to be without bumping actual version."
    )

    val startingVersion = settingKey[String](
      """This option defines starting version of the build in case there is no tag available to determine next version. 
        |Default value is "0.1.0-SNAPSHOT".""".stripMargin.replaceNewLines
    )

    val tagPrefix = settingKey[String](
      "This option defines prefix to use when tagging a release. Default value is \"v\"."
    )

    val tagPattern = settingKey[Regex](
      "This option defines the pattern to identify tag. Default value is \"\\d++\\.\\d++\\.\\d++\"."
    )

    val snapshotSuffix = settingKey[String](
      "This option defines the suffix for snapshot. Default value is \"SNAPSHOT\"."
    )

    val preReleasePrefix = settingKey[String](
      "This option defines the prefix for the preRelease. Default value is \"RC.\"."
    )

    val forceBump = settingKey[Boolean](
      s"""This option defines the flag to enable forceBump. Default value is "false". This option can be set
         | via system property "sbt.release.forceBump"."""
        .stripMargin
        .replaceNewLines
    )

    val newPreRelease = settingKey[Boolean](
      s"""This option defines the flag to enable new-pre-release. Default value is "false". This option can be set
         | via system property "sbt.release.newPreRelease" as well as via "\\[new-pre-release]" regular expression."""
        .stripMargin
        .replaceNewLines
    )

    val promoteToRelease = settingKey[Boolean](
      s"""This option defines the flag to enable promote-to-release. Default value is "false". This option can be set
         | via system property "sbt.release.newPreRelease" as well as via "\\[promote]" regular expression."""
        .stripMargin
        .replaceNewLines
    )

    val snapshot = settingKey[Boolean](
      s"""This option defines the flag to make current release a snapshot release. Default value is "true". This option
         | can be set via system property "sbt.release.snapshot"."""
        .stripMargin
        .replaceNewLines
    )

    val componentToBump = settingKey[Option[String]](
      s"""This option defines the component to bump. Default value is "NONE", which corresponds to bumping 
         |the lowest precedence component. Acceptable values are "PRE_RELEASE", "PATCH", "MINOR", "MAJOR", going from 
         |lowest precedence to highest precedence."""
        .stripMargin
        .replaceNewLines
    )

    val autoBumpMajorPattern = settingKey[Option[Regex]](
      s"""This option defines the regular expression to bump "MAJOR" component. Default value is "\\[major]"."""
    )

    val autoBumpMinorPattern = settingKey[Option[Regex]](
      s"""This option defines the regular expression to bump "MINOR" component. Default value is "\\[minor]"."""
    )

    val autoBumpPatchPattern = settingKey[Option[Regex]](
      s"""This option defines the regular expression to bump "PATCH" component. Default value is "\\[patch]"."""
    )

    val autoBumpNewPreReleasePattern =
      settingKey[Option[Regex]](
        s"""This option defines the regular expression to bump "PRE_RELEASE" component. Default value is
           | "\\[new-pre-release]".""".stripMargin.replaceNewLines
      )

    val autoBumpPromoteToReleasePattern =
      settingKey[Option[Regex]](
        s"""This option defines the regular expression to bump a . Default value is "\\[promote]"."""
      )

    val majorVersionsMatching = settingKey[Int](
      s"""This option defines filtering of a particular major version. Default value is "-1", which is 
         |corresponds to no matching.""".stripMargin.replaceNewLines
    )

    val minorVersionsMatching = settingKey[Int](
      s"""This option defines filtering of a particular minor version. Default value is "-1", which is 
         |corresponds to no matching."""
        .stripMargin
        .replaceNewLines
    )

    val patchVersionsMatching = settingKey[Int](
      s"""This option defines filtering of a particular patch version. Default value is "-1", which is 
         |corresponds to no matching."""
        .stripMargin
        .replaceNewLines
    )

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

  private def initializeReleaseVersionFile =
    Def.setting {
      val file = File.createTempFile("version_", ".sbt")
      val ver =
        if (releaseUseGlobalVersion.value)
          (version in ThisBuild).value
        else version.value

      file.deleteOnExit()
      IO.write(
        file,
        s"""version in ThisBuild := "$ver""""
      )
      file
    }

  override def projectSettings: Seq[Def.Setting[_]] = Seq[Setting[_]](
    releaseVersionFile := initializeReleaseVersionFile.value,
    startingVersion := getStartingVersion.value,
    tagPrefix := DefaultTagPrefix,
    tagPattern := DefaultTagPattern,
    snapshotSuffix := DefaultSnapshotSuffix,
    preReleasePrefix := DefaultPreReleasePrefix,
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
