package com.alphasystem
package sbt
package semver
package release
package internal

import sbtsemverrelease.AutoBump

class SetupVersionComponentsForBump {

  import VersionComponent.*

  private var result = NONE.getIndex

  def parseMessage(commitMessage: String, autoBump: AutoBump): SetupVersionComponentsForBump = {
    if (autoBump.major(commitMessage)) addMajor()
    if (autoBump.minor(commitMessage)) addMinor()
    if (autoBump.patch(commitMessage)) addPatch()
    if (autoBump.newPreRelease(commitMessage)) addNewPreRelease()
    if (autoBump.promoteToRelease(commitMessage)) addPromoteToRelease()
    this
  }

  private def addMajor(): SetupVersionComponentsForBump = addComponent(MAJOR)
  def removeMajor(): SetupVersionComponentsForBump = removeComponent(MAJOR)

  private def addMinor(): SetupVersionComponentsForBump = addComponent(MINOR)
  def removeMinor(): SetupVersionComponentsForBump = removeComponent(MINOR)

  def addPatch(): SetupVersionComponentsForBump = addComponent(PATCH)
  def removePatch(): SetupVersionComponentsForBump = removeComponent(PATCH)

  def addHotFix(): SetupVersionComponentsForBump = addComponent(HOT_FIX)
  def removeHotFix(): SetupVersionComponentsForBump = removeComponent(HOT_FIX)

  private def addNewPreRelease(): SetupVersionComponentsForBump = addComponent(NEW_PRE_RELEASE)
  def removeNewPreRelease(): SetupVersionComponentsForBump = removeComponent(NEW_PRE_RELEASE)

  def addPreRelease(): SetupVersionComponentsForBump = addComponent(PRE_RELEASE)
  def removePreRelease(): SetupVersionComponentsForBump = removeComponent(PRE_RELEASE)

  def addPromoteToRelease(): SetupVersionComponentsForBump = addComponent(PROMOTE_TO_RELEASE)
  def removePromoteToRelease(): SetupVersionComponentsForBump = removeComponent(PROMOTE_TO_RELEASE)

  def addSnapshot(): SetupVersionComponentsForBump = addComponent(SNAPSHOT)

  def hasMajor: Boolean = hasGivenComponent(MAJOR)
  def hasMinor: Boolean = hasGivenComponent(MINOR)
  def hasPromoteToRelease: Boolean = hasGivenComponent(PROMOTE_TO_RELEASE)
  def hasMandatoryComponents: Boolean = hasMajor || hasMinor || hasGivenComponent(PATCH)
  def hasEssentialComponents: Boolean = hasMajor || hasMinor || hasGivenComponent(PATCH) || hasPromoteToRelease ||
    hasGivenComponent(PRE_RELEASE) || hasGivenComponent(HOT_FIX)

  def isEmpty: Boolean = result == NONE.getIndex

  def reset(): SetupVersionComponentsForBump = {
    result = NONE.getIndex
    this
  }

  def getVersionComponents: Seq[VersionComponent] =
    Set(
      VersionComponent.fromIndex(result & MAJOR.getIndex),
      VersionComponent.fromIndex(result & MINOR.getIndex),
      VersionComponent.fromIndex(result & PATCH.getIndex),
      VersionComponent.fromIndex(result & HOT_FIX.getIndex),
      VersionComponent.fromIndex(result & NEW_PRE_RELEASE.getIndex),
      VersionComponent.fromIndex(result & PRE_RELEASE.getIndex),
      VersionComponent.fromIndex(result & PROMOTE_TO_RELEASE.getIndex),
      VersionComponent.fromIndex(result & SNAPSHOT.getIndex)
    ).toSeq.filterNot(_ == VersionComponent.NONE)

  def addComponentIfRequired(
    versionComponent: VersionComponent,
    condition: () => Boolean
  ): SetupVersionComponentsForBump = if (condition()) addComponent(versionComponent) else this

  private def addComponent(versionComponent: VersionComponent): SetupVersionComponentsForBump = {
    result = result | (versionComponent.getIndex | NONE.getIndex)
    this
  }

  private def removeComponent(versionComponent: VersionComponent): SetupVersionComponentsForBump = {
    result = result & ~(versionComponent.getIndex | NONE.getIndex)
    this
  }

  private def hasGivenComponent(versionComponent: VersionComponent) =
    (result & versionComponent.getIndex) == versionComponent.getIndex

  override def toString: String =
    s"""SetupVersionComponentsForBump(MAJOR = ${hasGivenComponent(VersionComponent.MAJOR)},
       |PROMOTE_TO_RELEASE = ${hasGivenComponent(VersionComponent.PROMOTE_TO_RELEASE)})""".stripMargin.replaceNewLines
}

object SetupVersionComponentsForBump {
  def apply(): SetupVersionComponentsForBump = new SetupVersionComponentsForBump()
}
