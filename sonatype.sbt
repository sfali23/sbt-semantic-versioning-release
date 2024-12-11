import xerial.sbt.Sonatype.GitHubHosting

import java.util.Base64

def getEnvVariable(name: String) = new String(Base64.getDecoder.decode(System.getenv(name).getBytes))

sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
sonatypeCredentialHost := "s01.oss.sonatype.org"
credentials += Credentials(
  realm = "Sonatype Nexus Repository Manager",
  host = "s01.oss.sonatype.org",
  userName = getEnvVariable("SONATYPE_USERNAME"),
  passwd = getEnvVariable("SONATYPE_PASSWORD")
)
publishTo := sonatypePublishToBundle.value
sonatypeProjectHosting := Some(
  GitHubHosting(
    "sfali23",
    "sbt-semantic-versioning-release",
    "f.syed.ali@gmail.com"
  )
)
