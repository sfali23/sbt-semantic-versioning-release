import sbtrelease.ReleaseStateTransformations._
import sbt.complete.DefaultParsers._

publishTo := Some(Resolver.file("file", new File(".")))

lazy val root = (project in file("."))
  .settings(
    organization := "com.example",
    name := "simple",
    //skip in publish := true,
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    releasePublishArtifactsAction := publishLocal.value
  )

val checkContentsOfVersionSbt =
  inputKey[Unit]("Check that the contents of version.sbt is as expected")
val parser = Space ~> StringBasic

checkContentsOfVersionSbt := {
  val expected = parser.parsed
  val versionFile = ((baseDirectory).value) / "version.sbt"
  assert(
    IO.read(versionFile).contains(expected),
    s"does not contains ${expected} in ${versionFile}"
  )
}
