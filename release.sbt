import ReleaseTransformations.*
import sbtrelease.ReleasePlugin.runtimeVersion

val checkSnapshotVersion = settingKey[Boolean]("Check if current version is snapshot")
checkSnapshotVersion := runtimeVersion.value.contains(snapshotConfig.value.prefix)

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

def releaseSteps(snapshot: Boolean) =
  initialSteps ++
    conditionalSteps(snapshot, tagRelease) ++
    publishingSteps ++
    conditionalSteps(snapshot, pushChanges)

releaseProcess := releaseSteps(checkSnapshotVersion.value)
