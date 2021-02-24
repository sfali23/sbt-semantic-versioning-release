import sbt.Keys.publishMavenStyle

lazy val `sbt-semantic-versioning-release` = project
  .in(file("."))
  .enablePlugins(SbtPlugin, ScalafmtPlugin)
  .settings(
    organization := "com.github.sfali23.sbt",
    name := "sbt-semver-release",
    version in ThisBuild := "0.1.0-SNAPSHOT",
    scalaVersion in ThisBuild := "2.12.12",
    // Don't update crossSbtVersions!
    // https://github.com/sbt/sbt/issues/5049
    crossSbtVersions := Vector("0.13.18", "1.1.6"),
    publishMavenStyle := false,
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
