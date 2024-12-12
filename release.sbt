import ReleaseTransformations.*

def initialSteps: Seq[ReleaseStep] = Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion
)

def publishingSteps: Seq[ReleaseStep] = Seq(
  publishArtifacts,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeBundleRelease")
)

releaseProcess := initialSteps ++ Seq[ReleaseStep](tagRelease) ++ publishingSteps ++ Seq[ReleaseStep](pushChanges)
