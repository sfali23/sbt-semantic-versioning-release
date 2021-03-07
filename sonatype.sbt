import xerial.sbt.Sonatype._

credentials += Credentials(Path.userHome / ".sbt" / "sonatype-credentials")
publishTo := sonatypePublishToBundle.value
sonatypeProjectHosting := Some(
  GitHubHosting(
    "sfali23",
    "sbt-semantic-versioning-release",
    "f.syed.ali@gmail.com"
  )
)
