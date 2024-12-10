package sbtsemverrelease

import com.alphasystem.sbt.semver.release.DefaultSnapshotSuffix

case class SnapshotConfig(
  suffix: String = DefaultSnapshotSuffix,
  appendCommitHash: Boolean = false,
  useShortHash: Boolean = false)
