import sbt.Keys.publishMavenStyle

lazy val `sbt-semantic-versioning-release` = project
  .in(file("."))
  .enablePlugins(SbtPlugin, ScalafmtPlugin)
  .settings(
    organization := "io.github.sfali23",
    name := "sbt-semver-release",
    version in ThisBuild := "0.1.0-SNAPSHOT",
    scalaVersion in ThisBuild := "2.12.12",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    // Don't update crossSbtVersions!
    // https://github.com/sbt/sbt/issues/5049
    crossSbtVersions := Vector("0.13.18", "1.1.6"),
    sbtVersion in ThisBuild := "1.4.4",
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
      "org.eclipse.jgit" % "org.eclipse.jgit"       % "5.10.0.202012080955-r",
      "io.circe"        %% "circe-core"             % "0.13.0"                % Test,
      "io.circe"        %% "circe-generic"          % "0.13.0"                % Test,
      "io.circe"        %% "circe-parser"           % "0.13.0"                % Test,
      "org.eclipse.jgit" % "org.eclipse.jgit.junit" % "5.10.0.202012080955-r" % Test,
      "org.scalatest"   %% "scalatest"              % "3.3.0-SNAP3"           % Test
    )
  )
