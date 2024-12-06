package sbtsemverrelease

import com.alphasystem.sbt.semver.release.*
import com.alphasystem.sbt.semver.release.common.JGitAdapter
import com.alphasystem.sbt.semver.release.internal.*
import sbt.Keys.*
import sbt.*
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.*
import sbtrelease.ReleaseStateTransformations.*

import java.io.File
import scala.util.matching.Regex
import scala.util.{Properties, Try}

object SemanticVersioningReleasePlugin extends AutoPlugin {

  object autoImport {

    val determineVersion = taskKey[String](
      "A task to determine the next version to be without bumping actual version."
    )

    val startingVersion = settingKey[String](
      """This option defines the starting version of the build in case there is no tag available to determine next version. 
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
         | via system property "sbt.release.promoteToRelease" as well as via "\\[promote]" regular expression."""
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
         |lowest precedence to highest precedence. This option can be set via system property 
         |"sbt.release.componentToBump"."""
        .stripMargin
        .replaceNewLines
    )

    val autoBump = settingKey[AutoBump](
      "This option allows you to specify how the build version should be automatically bumped based on the contents of commit messages."
    )

    val preRelease = settingKey[PreReleaseConfig](
      "This option defines configuration for pre-release"
    )

    object ReleaseKeys {
      val versionToBe = AttributeKey[String]("Next version")
    }
  }

  import autoImport.*

  override def trigger = allRequirements

  override def requires: Plugins = ReleasePlugin

  private def toConfiguration =
    Def.task {
      SemanticBuildVersionConfiguration(
        startingVersion = startingVersion.value,
        tagPrefix = tagPrefix.value,
        snapshotSuffix = snapshotSuffix.value,
        forceBump = forceBump.value,
        promoteToRelease = promoteToRelease.value,
        snapshot = snapshot.value,
        newPreRelease = newPreRelease.value,
        autoBump = autoBump.value,
        defaultBumpLevel = DefaultBumpLevel, // TODO: populate this
        componentToBump = componentToBump
          .value
          .map(VersionComponent.valueOf)
          .getOrElse(DefaultComponentToBump),
        preReleaseConfig = preRelease.value,
        hotfixBranchPattern = DefaultHotfixBranchPattern, // TODO: populate this
        extraReleaseBranches = Seq.empty // TODO: populate this
      )
    }

  private def initializeSettingFromSystemProperty(
    propertyName: String,
    defaultValue: Boolean
  ) =
    Properties
      .propOrNone(propertyName)
      .map(_.toBoolean)
      .getOrElse(defaultValue)

  private def getStartingVersion =
    Def.setting {
      val versionInBuild = (ThisBuild / version).value
      val parts = versionInBuild.split("-").toList
      parts match {
        case Nil => DefaultStartingVersion

        case head :: _ =>
          if (VersioningHelper.isValidStartingVersion(head))
            head
          else DefaultStartingVersion
      }
    }

  private def initializeSnapshot =
    // JGitAdapter doesn't work well in tests where git is not initialized
    Def.setting {
      val computedValue =
        Try(JGitAdapter(baseDirectory.value))
          .toOption
          .map(_.hasUncommittedChanges)
          .getOrElse(DefaultSnapshot)
      initializeSettingFromSystemProperty(
        SnapshotSystemPropertyName,
        computedValue
      )
    }

  private def initializeReleaseVersionFile =
    Def.setting {
      val defaultFile = new File(baseDirectory.value, "version.sbt")
      if (defaultFile.exists()) defaultFile
      else {
        val file = File.createTempFile("version_", ".sbt")
        val ver =
          if (releaseUseGlobalVersion.value)
            (ThisBuild / version).value
          else version.value

        file.deleteOnExit()
        IO.write(
          file,
          s"""version in ThisBuild := "$ver""""
        )
        file
      }
    }

  override def projectSettings: Seq[Def.Setting[?]] = Seq[Setting[?]](
    releaseVersionFile := initializeReleaseVersionFile.value,
    startingVersion := getStartingVersion.value,
    tagPrefix := DefaultTagPrefix,
    tagPattern := DefaultTagPattern,
    snapshotSuffix := DefaultSnapshotSuffix,
    forceBump := initializeSettingFromSystemProperty(
      ForceBumpSystemPropertyName,
      DefaultForceBump
    ),
    promoteToRelease := initializeSettingFromSystemProperty(
      PromoteToReleaseSystemPropertyName,
      DefaultPromoteToRelease
    ),
    snapshot := initializeSnapshot.value,
    newPreRelease := initializeSettingFromSystemProperty(
      NewPreReleaseSystemPropertyName,
      DefaultNewPreRelease
    ),
    componentToBump := Some(
      Properties
        .propOrNone(ComponentToBumpSystemPropertyName)
        .getOrElse(DefaultComponentToBump.name())
    ),
    autoBump := AutoBump(),
    preRelease := PreReleaseConfig(),
    determineVersion := {
      val config = toConfiguration.value
      SemanticBuildVersion(
        baseDirectory.value,
        config
      ).determineVersion.toStringValue(config.tagPrefix)
    },
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
      publishArtifacts,
      pushChanges
    )
  )
}
