import xerial.sbt.Sonatype.{GitHubHosting, sonatypeCentralHost}

sonatypeCredentialHost := sonatypeCentralHost
credentials += Credentials(
  realm = "Sonatype Central Repository",
  host = sonatypeCentralHost,
  userName = System.getenv("SONATYPE_USERNAME"),
  passwd = System.getenv("SONATYPE_PASSWORD")
)
publishTo := localStaging.value
sonatypeProjectHosting := Some(
  GitHubHosting(
    "sfali23",
    "sbt-semantic-versioning-release",
    "f.syed.ali@gmail.com"
  )
)
