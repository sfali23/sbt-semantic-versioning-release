libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

resolvers += "Sonatype OSS" at "https://s01.oss.sonatype.org/content/groups/public/"

addSbtPlugin("org.scalameta"     % "sbt-scalafmt"        % "2.4.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-native-packager" % "1.8.0")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"        % "3.9.7")
addSbtPlugin("com.github.sbt"    % "sbt-pgp"             % "2.1.2")
addSbtPlugin("io.github.sfali23" % "sbt-semver-release"  % "0.2.0")
