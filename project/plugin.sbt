libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("org.scalameta"          % "sbt-scalafmt"        % "2.4.0")
addSbtPlugin("com.typesafe.sbt"       % "sbt-native-packager" % "1.8.0")
addSbtPlugin("org.xerial.sbt"         % "sbt-sonatype"        % "3.9.7")
addSbtPlugin("com.github.sbt"         % "sbt-pgp"             % "2.1.2")
addSbtPlugin("com.github.sfali23.sbt" % "sbt-semver-release"  % "0.1.0-SNAPSHOT")
