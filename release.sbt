import ReleaseTransformations.*

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("^ ct"),
  setReleaseVersion,
  // tagRelease,
  publishArtifacts,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeBundleRelease")
  // pushChanges
)
