import sbtrelease.ReleasePlugin.autoImport.releaseVersionFile
import ReleaseTransformations._
import sbtsemverrelease.AutoBump
import sbt.complete.DefaultParsers._

publishTo := Some(Resolver.file("file", new File(".")))

lazy val root = (project in file("."))
  .settings(
    organization := "com.example",
    name := "simple",
    version in ThisBuild := "0.1.0-SNAPSHOT",
    autoBump := AutoBump(majorPattern = Some("_major_".r)),
    Global / onChangedBuildSource := ReloadOnSourceChanges,
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

val checkContentsOfVersionSbt =
  inputKey[Unit]("Check that the contents of version.sbt is as expected")
val parser = Space ~> StringBasic

checkContentsOfVersionSbt := {
  val expected = parser.parsed
  val versionFile = releaseVersionFile.value
  assert(
    IO.read(versionFile).contains(expected),
    s"does not contains $expected in $versionFile"
  )
}
