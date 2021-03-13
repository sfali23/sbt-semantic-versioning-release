import xerial.sbt.Sonatype._

sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
credentials += Credentials(Path.userHome / ".sbt" / "sonatype-credentials")
publishTo := sonatypePublishToBundle.value
sonatypeProjectHosting := Some(
  GitHubHosting(
    "sfali23",
    "sbt-semantic-versioning-release",
    "f.syed.ali@gmail.com"
  )
)
