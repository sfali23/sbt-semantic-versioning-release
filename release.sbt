import ReleaseTransformations.*

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

releaseProcess := initialSteps ++
  Seq[ReleaseStep](tagRelease) ++
  publishingSteps ++
  Seq[ReleaseStep](pushChanges)
