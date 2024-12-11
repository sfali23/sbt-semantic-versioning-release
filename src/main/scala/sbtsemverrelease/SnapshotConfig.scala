package sbtsemverrelease

import com.alphasystem.sbt.semver.release.DefaultSnapshotPrefix

case class SnapshotConfig(
  prefix: String = DefaultSnapshotPrefix,
  appendCommitHash: Boolean = true,
  useShortHash: Boolean = true)
