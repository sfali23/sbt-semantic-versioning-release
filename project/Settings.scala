import sbt.*
import sbt.librarymanagement.ModuleID

object Settings {

  object V {
    val CucumberExpressions = "18.0.1"
    val CucumberJunit = "7.30.0"
    val CucumberScala = "8.33.0"
    val EclipseJGit = "7.3.0.202506031305-r"
    val LogbackClassic = "1.5.19"
    val ScalaTest = "3.3.0-SNAP4"
    val Slf4jApi = "2.0.17"
    val TypesafeConfig = "1.4.5"
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
