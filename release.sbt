import ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  releaseStepCommandAndRemaining("^ scripted"),
  setReleaseVersion,
  tagRelease,
  publishArtifacts,
  pushChanges
)
