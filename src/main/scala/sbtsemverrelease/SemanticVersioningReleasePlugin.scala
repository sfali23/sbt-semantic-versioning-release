package sbtsemverrelease

import com.alphasystem.sbt.semver.release.*
import com.alphasystem.sbt.semver.release.internal.*
import sbt.Keys.*
import sbt.*
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.*
import sbtrelease.ReleasePlugin.runtimeVersion
import sbtrelease.ReleaseStateTransformations.*

import java.io.File
import scala.util.matching.Regex
import scala.util.{Properties, Try}

object SemanticVersioningReleasePlugin extends AutoPlugin {

  object autoImport {

    private[sbtsemverrelease] val determineVersionInternal =
      taskKey[String]("Internal task to call underlying system to get next version.")

    val determineVersion = taskKey[Unit]("A task to determine the next version to be without bumping actual version.")

    val startingVersion = settingKey[String](
      """This option defines the starting version of the build in case there is no tag available to determine next version. 
        |Default value is "0.1.0-SNAPSHOT".""".stripMargin.replaceNewLines
    )

    val tagPrefix = settingKey[String](
      "This option defines prefix to use when tagging a release. Default value is \"v\"."
    )

    val forceBump = settingKey[Boolean](
      """This option defines the flag to enable forceBump. Default value is "false". This option can be set
        | via system property "sbt.release.forceBump"."""
        .stripMargin
        .replaceNewLines
    )

    val newPreRelease = settingKey[Boolean](
      """This option defines the flag to enable new-pre-release. Default value is "false". This option can be set
        | via system property "sbt.release.newPreRelease" as well as via "\\[new-pre-release]" regular expression."""
        .stripMargin
        .replaceNewLines
    )

    val promoteToRelease = settingKey[Boolean](
      """This option defines the flag to enable promote-to-release. Default value is "false". This option can be set
        | via system property "sbt.release.promoteToRelease" as well as via "\\[promote]" regular expression."""
        .stripMargin
        .replaceNewLines
    )

    val snapshot = settingKey[Boolean](
      """This option defines the flag to make current release a snapshot release. Default value is "true". This option
        | can be set via system property "sbt.release.snapshot"."""
        .stripMargin
        .replaceNewLines
    )

    val defaultBumpLevel = settingKey[ComponentToBump](
      """This option defines the default level to bump, if bumping of version is required and no bump pattern can be
        |deduced from commit messages. The default value is 'PATCH. This option can be set via system property
        |"sbt.release.defaultBumpLevel"."""
        .stripMargin
        .replaceNewLines
    )

    val componentToBump = settingKey[Option[ComponentToBump]](
      s"""This option defines the component to bump. Default value is "NONE", which corresponds to bumping 
         |the lowest precedence component. Acceptable values are "MAJOR", "MINOR", "PATCH", going from
         |lowest precedence to highest precedence. This option is only applicable for "forceBump" strategy.
         |This option can be set via system property "sbt.release.componentToBump"."""
        .stripMargin
        .replaceNewLines
    )

    val autoBump = settingKey[AutoBump](
      "This option allows you to specify how the build version should be automatically bumped based on the contents of commit messages."
    )

    val snapshotConfig = settingKey[SnapshotConfig]("This option defines configuration for snapshot")

    val preRelease = settingKey[PreReleaseConfig]("This option defines configuration for pre-release")

    val hotfixBranchPattern =
      settingKey[Regex]("""This option defines branch name pattern for hot fix branches. Default pattern is
                          |"^v(0|[1-9]\d*)\\.(0|[1-9]\d*)\\.(0|[1-9]\d*)\+$" This option can be set via system property
                          | "sbt.release.hotFixBranchPattern".""".stripMargin)

    val extraReleaseBranches =
      settingKey[Seq[String]](
        """This option defines the list of name of branches that can be used to release from. By default only "main" and
          | "master" branches can be used for publishing, if plugin is executed from a branch then it would created a
          | snapshot version. This rule doesn't apply for hot fix branches. Possible use case is to a "development" branch."""
          .stripMargin
          .replaceNewLines
      )

    val addUnReleasedCommitsToTagSummary = settingKey[Boolean]("Add un released commits to tag summary.")
  }

  import autoImport.*

  override def trigger = allRequirements

  override def requires: Plugins = ReleasePlugin

  private def toConfiguration =
    Def.task {
      SemanticBuildVersionConfiguration(
        startingVersion = startingVersion.value,
        tagPrefix = tagPrefix.value,
        forceBump = forceBump.value,
        promoteToRelease = promoteToRelease.value,
        snapshot = snapshot.value,
        newPreRelease = newPreRelease.value,
        autoBump = autoBump.value,
        defaultBumpLevel = defaultBumpLevel.value.toVersionComponent,
        componentToBump = componentToBump
          .value
          .map(_.toVersionComponent)
          .getOrElse(DefaultComponentToBump),
        snapshotConfig = snapshotConfig.value,
        preReleaseConfig = preRelease.value,
        hotfixBranchPattern = hotfixBranchPattern.value,
        extraReleaseBranches = extraReleaseBranches.value
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
          if (isValidStartingVersion(head))
            head
          else DefaultStartingVersion
      }
    }

  private def initializeSnapshot =
    Def.setting {
      initializeSettingFromSystemProperty(
        SnapshotSystemPropertyName,
        DefaultBooleanValue
      )
    }

  private def initializeDefaultBumpLevel =
    Def.setting {
      val name = Properties.propOrElse(DefaultBumpLevelSystemPropertyName, ComponentToBump.PATCH.name())
      Try(ComponentToBump.valueOf(name)).getOrElse(ComponentToBump.PATCH)
    }

  private def initializeComponentToBump =
    Def.setting {
      Properties
        .propOrNone(ComponentToBumpSystemPropertyName)
        .flatMap(name => Try(ComponentToBump.valueOf(name)).toOption)
    }

  private def initializeDefaultHotFixBranchPattern =
    Def.setting {
      Properties.propOrNone(HotfixBranchPatternSystemPropertyName) match {
        case Some(value) => s"$value".r
        case None        => initializeHotfixBranchPattern(tagPrefix.value)
      }
    }

  private def initializeAddUnReleasedCommitsToTagSummary =
    Def.setting {
      initializeSettingFromSystemProperty(
        AddUnReleasedCommitsToTagSummarySystemPropertyName,
        DefaultBooleanValue
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
    forceBump := initializeSettingFromSystemProperty(
      ForceBumpSystemPropertyName,
      DefaultBooleanValue
    ),
    promoteToRelease := initializeSettingFromSystemProperty(
      PromoteToReleaseSystemPropertyName,
      DefaultBooleanValue
    ),
    snapshot := initializeSnapshot.value,
    newPreRelease := initializeSettingFromSystemProperty(
      NewPreReleaseSystemPropertyName,
      DefaultBooleanValue
    ),
    defaultBumpLevel := initializeDefaultBumpLevel.value,
    componentToBump := initializeComponentToBump.value,
    autoBump := AutoBump(),
    snapshotConfig := SnapshotConfig(),
    preRelease := PreReleaseConfig(),
    hotfixBranchPattern := initializeDefaultHotFixBranchPattern.value,
    extraReleaseBranches := Seq.empty[String],
    addUnReleasedCommitsToTagSummary := initializeAddUnReleasedCommitsToTagSummary.value,
    determineVersionInternal := {
      SemanticBuildVersion(
        baseDirectory.value,
        toConfiguration.value
      ).determineVersion
    },
    determineVersion := {
      scala.Console.println(s"Next determined version is: ${scala.Console.BOLD}${scala.Console.RED}${determineVersionInternal.value}${scala.Console.RESET}")
    },
    releaseTagName := s"${tagPrefix.value}${runtimeVersion.value}",
    releaseTagComment := {
      if (addUnReleasedCommitsToTagSummary.value) {
        val config = toConfiguration.value
        val semanticBuildVersion = SemanticBuildVersion(baseDirectory.value, config)
        val latestVersion = semanticBuildVersion.latestVersion.map(_.toStringValue).getOrElse(config.startingVersion)
        val commits = semanticBuildVersion.getUnReleasedCommits(latestVersion).mkString(System.lineSeparator())
        Seq(releaseTagComment.value, commits).mkString(s"${System.lineSeparator()}${System.lineSeparator()}")
      } else releaseTagComment.value
    },
    releaseVersion := { _ => determineVersionInternal.value: @sbtUnchecked },
    releaseNextVersion := { _ => "" },
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
