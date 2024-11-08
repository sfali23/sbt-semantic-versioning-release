import sbt.Keys.publishMavenStyle

lazy val `sbt-semantic-versioning-release` = project
  .in(file("."))
  .enablePlugins(SbtPlugin, ScalafmtPlugin)
  .settings(
    organization := "io.github.sfali23",
    name := "sbt-semver-release",
    ThisBuild / version := "0.1.0-SNAPSHOT",
    ThisBuild / scalaVersion := "2.12.20",
    javacOptions ++= Seq("-source", "17", "-target", "17"),
    // Don't update crossSbtVersions!
    // https://github.com/sbt/sbt/issues/5049
    // crossSbtVersions := Vector("0.13.18", "1.1.6"),
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
    addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13"),
    libraryDependencies ++= Seq(
      "org.eclipse.jgit" % "org.eclipse.jgit"       % "7.0.0.202409031743-r",
      "io.circe"        %% "circe-core"             % "0.14.10"              % Test,
      "io.circe"        %% "circe-generic"          % "0.14.10"              % Test,
      "io.circe"        %% "circe-parser"           % "0.14.10"              % Test,
      "org.eclipse.jgit" % "org.eclipse.jgit.junit" % "7.0.0.202409031743-r" % Test,
      "org.scalatest"   %% "scalatest"              % "3.3.0-SNAP4"          % Test
    )
  )
