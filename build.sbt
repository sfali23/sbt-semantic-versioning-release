import sbt.Keys.publishMavenStyle
import Settings.*

lazy val `sbt-semantic-versioning-release` = project
  .in(file("."))
  .enablePlugins(SbtPlugin, ScalafmtPlugin, CucumberPlugin, DependencyUpdaterPlugin)
  .settings(
    organization := "io.github.sfali23",
    name := "sbt-semver-release",
    ThisBuild / version := "0.1.0-SNAPSHOT",
    ThisBuild / scalaVersion := "2.12.20",
    javacOptions ++= Seq("-source", "17", "-target", "17"),
    publishMavenStyle := true,
    licenses := Seq(
      "APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    developers := List(
      Developer(
        id = "sfali23",
        name = "Syed Farhan Ali",
        email = "f.syed.ali@gmail.com",
        url = url("https://github.com/sfali23/sbt-semantic-versioning-release")
      )
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-language:implicitConversions"
    ),
    scriptedBufferLog := false,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq(
        "-Xmx1024M",
        "-Dsbt.ivy.home=" + sbt.Keys.ivyPaths.value.ivyHome.getOrElse("~/.ivy2")
      )
    },
    CucumberPlugin.glues := List("steps"),
    addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0"),
    libraryDependencies ++= Dependencies
  )

addCommandAlias("ct", "clean; test; cucumber; scripted;")
