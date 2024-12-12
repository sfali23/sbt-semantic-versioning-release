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

releaseProcess := initialSteps ++ {
  if (version.value.contains("SNAPSHOT")) Seq.empty[ReleaseStep]
  else Seq[ReleaseStep](tagRelease)
} ++ publishingSteps ++ {
  if (version.value.contains("SNAPSHOT")) Seq.empty[ReleaseStep]
  else Seq[ReleaseStep](pushChanges)
}
