import xerial.sbt.Sonatype.GitHubHosting

sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
sonatypeCredentialHost := "s01.oss.sonatype.org"
credentials += Credentials(
  realm = "Sonatype Nexus Repository Manager",
  host = "s01.oss.sonatype.org",
  userName = System.getenv("SONATYPE_USERNAME"),
  passwd = System.getenv("SONATYPE_PASSWORD")
)
publishTo := sonatypePublishToBundle.value
sonatypeProjectHosting := Some(
  GitHubHosting(
    "sfali23",
    "sbt-semantic-versioning-release",
    "f.syed.ali@gmail.com"
  )
)
