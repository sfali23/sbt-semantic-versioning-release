import ReleaseTransformations.*

val checkSnapshotVersion = settingKey[Boolean]("Check if current version is snapshot")
checkSnapshotVersion := IO.read(releaseVersionFile.value).contains(snapshotConfig.value.prefix)

lazy val initialSteps: Seq[ReleaseStep] = Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion
)

lazy val publishingSteps: Seq[ReleaseStep] = Seq(
  publishArtifacts,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeBundleRelease")
)

def conditionalSteps(cond: Boolean, steps: ReleaseStep*): Seq[ReleaseStep] =
  if (cond) Seq.empty[ReleaseStep] else steps

releaseProcess := initialSteps ++
  conditionalSteps(checkSnapshotVersion.value, tagRelease) ++
  publishingSteps ++
  conditionalSteps(checkSnapshotVersion.value, pushChanges)
