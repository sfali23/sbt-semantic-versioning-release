package com.alphasystem.sbt.semver.release.common

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{ Constants, ObjectId, Ref, Repository }
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.PushResult

import java.io.File
import scala.collection.JavaConverters._

class JGitAdapter(workingDir: File) {
  private val _repository: Repository = JGitAdapter.initRepository(workingDir)

  lazy val repository: Repository = _repository
  lazy val git = new Git(_repository)

  def directory: File = _repository.getDirectory

  def getHeadCommit: ObjectId = repository.resolve(Constants.HEAD)

  def getRevWalk: RevWalk = new RevWalk(repository)

  def getTags: Map[String, Ref] = _repository
    .getRefDatabase
    .getRefsByPrefix(Constants.R_TAGS)
    .asScala
    .toList
    .map { ref =>
      ref.getName.replaceAll(Constants.R_TAGS, "") -> ref
    }
    .toMap

  def hasUncommittedChanges: Boolean = git.status().call().hasUncommittedChanges

  def push(pushTag: Boolean = true): List[PushResult] = {
    val pushCommand = git.push()
    if (pushTag) {
      pushCommand.setPushTags()
    }
    pushCommand.call().asScala.toList
  }
}

object JGitAdapter {
  def apply(workingDir: File): JGitAdapter = new JGitAdapter(workingDir)

  def initRepository(
    workingDir: File,
    initialize: Boolean = false
  ): Repository = {
    if (initialize) {
      Git.init().setDirectory(workingDir).call()
    }
    val builder = new FileRepositoryBuilder()
      .setWorkTree(workingDir)
      .findGitDir(workingDir)

    val gitDir: File = builder.getGitDir
    if (Option(gitDir).isDefined && !gitDir.exists()) {
      throw new RuntimeException(
        s"Unable to find Git repository in: $workingDir"
      )
    }

    if (gitDir.getParentFile.getAbsolutePath != workingDir.getAbsolutePath) {
      builder.setWorkTree(gitDir.getParentFile)
    }
    builder.build()
  }
}
