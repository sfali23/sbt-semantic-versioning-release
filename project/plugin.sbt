libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("org.scalameta"    % "sbt-scalafmt"        % "2.4.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.0")
