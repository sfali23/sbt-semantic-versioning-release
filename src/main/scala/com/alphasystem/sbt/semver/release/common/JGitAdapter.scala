package com.alphasystem
package sbt
package semver
package release
package common

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{Constants, ObjectId, Repository}
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import java.io.File
import scala.collection.JavaConverters.*
import scala.util.Try

class JGitAdapter(workingDir: File) {
  private val _repository: Repository = JGitAdapter.initRepository(workingDir)

  lazy val repository: Repository = _repository
  lazy val git = new Git(_repository)

  def getHeadCommit: ObjectId = repository.resolve(Constants.HEAD)

  private def getShortHash(id: ObjectId): String = repository.newObjectReader().abbreviate(id).name()

  def getShortHash: String = getShortHash(getHeadCommit)

  def getCurrentBranch: String = repository.getBranch

  def getRevWalk: RevWalk = new RevWalk(repository)

  def getTagsForCurrentBranch: Seq[String] = {
    val tags =
      git.tagList().call().asScala.groupBy { tagRef =>
        getRevWalk.parseCommit(tagRef.getNonNullObjectId).getId
      }

    Try(repository.resolve(repository.getBranch)).toOption match {
      case Some(ref) if Option(ref).isDefined =>
        git
          .log()
          .add(ref)
          .call()
          .asScala
          .flatMap(rev => tags.get(rev.getId))
          .flatten
          .toList
          .map(_.getName.replaceAll(Constants.R_TAGS, ""))

      case _ => Seq.empty
    }
  }

  def getCommits: List[String] =
    git
      .log()
      .add(repository.resolve(repository.getBranch))
      .call()
      .asScala
      .map(_.getFullMessage)
      .toList

  def getUnReleasedCommits(
    start: String,
    end: String = Constants.HEAD
  ): List[String] = {
    val startId = repository.resolve(start)
    val endId = repository.resolve(end)
    val walk = getRevWalk
    (Try(walk.parseCommit(startId)).toOption, Try(walk.parseCommit(endId)).toOption) match {
      case (Some(startCommit), Some(endCommit)) =>
        val commits = git.log().addRange(startCommit, endCommit).call()
        commits
          .asScala
          .map { commit =>
            val shortHash = getShortHash(commit.getId)
            val message = commit.getShortMessage
            s"Commit($shortHash, $message)"
          }
          .toList

      case _ => List.empty
    }
  }

  def getCommitBetween(
    start: String,
    end: String = Constants.HEAD
  ): List[String] = {
    val startId = repository.resolve(start)
    val endId = repository.resolve(end)
    val walk = getRevWalk
    (Try(walk.parseCommit(startId)).toOption, Try(walk.parseCommit(endId)).toOption) match {
      case (Some(startCommit), Some(endCommit)) =>
        val commits = git.log().addRange(startCommit, endCommit).call()
        commits.asScala.map(_.getFullMessage).toList

      case _ => List.empty
    }
  }

  def hasUncommittedChanges: Boolean = git.status().call().hasUncommittedChanges
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
    if (Option(gitDir).isEmpty || !gitDir.exists()) {
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
