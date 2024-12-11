import ReleaseTransformations.*

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  // tagRelease,
  publishArtifacts,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeBundleRelease")
  // pushChanges
)
