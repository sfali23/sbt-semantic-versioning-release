import sbtrelease.ReleaseStateTransformations.{checkSnapshotDependencies, inquireVersions, publishArtifacts, pushChanges, runClean, runTest, setReleaseVersion, tagRelease}

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
  releaseStepCommand("sonaUpload"),
  releaseStepCommand("sonaRelease")
)

releaseProcess := initialSteps ++
  Seq[ReleaseStep](tagRelease) ++
  publishingSteps ++
  Seq[ReleaseStep](pushChanges)
