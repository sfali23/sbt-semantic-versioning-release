import sbt.Keys.publishMavenStyle

lazy val `sbt-semantic-versioning-release` = project
  .in(file("."))
  .enablePlugins(SbtPlugin, ScalafmtPlugin, CucumberPlugin)
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
    addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13"),
    libraryDependencies ++= Seq(
      "org.eclipse.jgit" % "org.eclipse.jgit" % "7.1.0.202411261347-r",
      "org.slf4j" % "slf4j-api" % "2.0.16",
      "ch.qos.logback" % "logback-classic" % "1.5.12",
      "io.circe" %% "circe-core" % "0.14.10" % Test,
      "io.circe" %% "circe-generic" % "0.14.10" % Test,
      "io.circe" %% "circe-parser" % "0.14.10" % Test,
      "org.eclipse.jgit" % "org.eclipse.jgit.junit" % "7.1.0.202411261347-r" % Test,
      "com.typesafe" % "config" % "1.4.3" % Test,
      "org.scalatest" %% "scalatest" % "3.3.0-SNAP4" % Test,
      "io.cucumber" %% "cucumber-scala" % "8.25.1" % Test,
      "io.cucumber" % "cucumber-junit" % "7.20.1" % Test,
      "io.cucumber" % "cucumber-expressions" % "18.0.1" % Test
    )
  )

addCommandAlias("ct", "clean; test; cucumber; scripted;")
