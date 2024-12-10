libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

resolvers += "Sonatype OSS" at "https://s01.oss.sonatype.org/content/groups/public/"

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.4")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.12.2")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.0")
addSbtPlugin("io.github.sfali23" % "sbt-semver-release" % "0.2.0-SNAPSHOT")
addSbtPlugin(
  "com.waioeka.sbt" % "cucumber-plugin" % "0.3.1" excludeAll
    ExclusionRule(organization = "org.scala-lang.modules", name = "scala-xml_2.12")
)
