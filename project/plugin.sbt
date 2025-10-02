libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.12.2")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("com.awwsmm.sbt" % "sbt-dependency-updater" % "0.4.0")
addSbtPlugin("io.github.sfali23" % "sbt-semver-release" % "0.5.4")
addSbtPlugin(
  "com.waioeka.sbt" % "cucumber-plugin" % "0.3.1" excludeAll
    ExclusionRule(organization = "org.scala-lang.modules", name = "scala-xml_2.12")
)
