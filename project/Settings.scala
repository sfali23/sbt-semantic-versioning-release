import sbt.*
import sbt.librarymanagement.ModuleID

object Settings {

  object V {
    val CucumberExpressions = "18.0.1"
    val CucumberJunit = "7.20.1"
    val CucumberScala = "8.25.1"
    val EclipseJGit = "7.1.0.202411261347-r"
    val LogbackClassic = "1.5.12"
    val ScalaTest = "3.3.0-SNAP4"
    val Slf4jApi = "2.0.16"
    val TypesafeConfig = "1.4.3"
  }

  val Dependencies: Seq[ModuleID] = Seq(
    "org.eclipse.jgit" % "org.eclipse.jgit" % V.EclipseJGit,
    "org.slf4j" % "slf4j-api" % V.Slf4jApi,
    "ch.qos.logback" % "logback-classic" % V.LogbackClassic,
    "org.eclipse.jgit" % "org.eclipse.jgit.junit" % V.EclipseJGit % Test,
    "com.typesafe" % "config" % V.TypesafeConfig % Test,
    "org.scalatest" %% "scalatest" % V.ScalaTest % Test,
    "io.cucumber" %% "cucumber-scala" % V.CucumberScala % Test,
    "io.cucumber" % "cucumber-junit" % V.CucumberJunit % Test,
    "io.cucumber" % "cucumber-expressions" % V.CucumberExpressions % Test
  )
}
