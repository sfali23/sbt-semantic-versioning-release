lazy val `sbt-semantic-versioning-release` = project
  .in(file("."))
  .withId("sbt-semantic-versioning-release")
  .enablePlugins(SbtPlugin, ScalafmtPlugin, GitPlugin)
  .settings(
    scalaVersion in ThisBuild := "2.12.12",
    libraryDependencies ++= Seq(
      "org.eclipse.jgit" % "org.eclipse.jgit"       % "5.10.0.202012080955-r",
      "io.circe"        %% "circe-core"             % "0.13.0"                % Test,
      "io.circe"        %% "circe-generic"          % "0.13.0"                % Test,
      "io.circe"        %% "circe-parser"           % "0.13.0"                % Test,
      "org.eclipse.jgit" % "org.eclipse.jgit.junit" % "5.10.0.202012080955-r" % Test,
      "org.scalatest"   %% "scalatest"              % "3.3.0-SNAP3"           % Test
    )
  )
