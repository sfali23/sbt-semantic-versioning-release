import ReleaseTransformations.*

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("^ ct"),
  setReleaseVersion
)
