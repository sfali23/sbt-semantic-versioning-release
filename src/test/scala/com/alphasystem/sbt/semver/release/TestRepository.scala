package com.alphasystem.sbt.semver.release

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{ Constants, Repository }
import org.eclipse.jgit.revwalk.{ RevTag, RevWalk }
import org.eclipse.jgit.transport.URIish

import java.io.{ File, PrintWriter }
import scala.util.Random

class TestRepository(val repository: Repository) {

  val git = new Git(repository)

  private val parentFile = repository.getDirectory.getParentFile.getAbsolutePath

  def commitAndTag(tag: String, annotated: Boolean = false): TestRepository = {
    git
      .commit()
      .setAuthor("Batman", "batman@waynemanor.com")
      .setMessage("blah")
      .call()

    git.tag().setAnnotated(annotated).setName(tag).call()

    this
  }

  def tag(tag: String, annotated: Boolean = false): TestRepository = {
    git.tag().setAnnotated(annotated).setName(tag).call()
    this
  }

  def commit(): TestRepository = commit("blah")

  def commit(message: String): TestRepository = {
    git
      .commit()
      .setAuthor("Batman", "batman@waynemanor.com")
      .setMessage(message)
      .call()
    this
  }

  def makeChanges(): TestRepository = {
    val fileName = s"file-${Random.alphanumeric.take(5).mkString("")}"
    val file = new File(parentFile, fileName)
    val writer = new PrintWriter(file)
    writer.write(Random.alphanumeric.take(50).mkString(""))
    writer.close()

    git.add().addFilepattern(fileName).call()
    this
  }

  def add(filePattern: String): TestRepository = {
    git.add().addFilepattern(filePattern).call()
    this
  }

  def checkoutBranch(branch: String): TestRepository = {
    git.checkout().setName(branch).call()
    this
  }

  def checkout(revString: String): TestRepository =
    checkoutBranch(repository.resolve(revString).name())

  def branch(name: String): TestRepository = {
    git.branchCreate().setName(name).call()
    this
  }

  def merge(target: String): TestRepository = {
    git.merge().include(repository.resolve(target)).call()
    this
  }

  def setOrigin(origin: TestRepository): TestRepository = {
    git
      .remoteAdd()
      .setName("origin")
      .setUri(new URIish(origin.repository.getDirectory.toURI.toURL))
    this
  }

  def getHeadTag: String =
    git.describe().setTarget(repository.resolve(Constants.HEAD)).call()

  def isHeadTagAnnotated: RevTag =
    new RevWalk(repository)
      .parseAny(repository.resolve(getHeadTag))
      .asInstanceOf[RevTag]

  def getHeadTagMessage: Option[String] = {
    val revWalk = new RevWalk(repository)
    val tagObject = revWalk.parseAny(repository.resolve(getHeadTag))
    tagObject match {
      case tag: RevTag => Some(tag.getFullMessage)
      case _           => None
    }
  }
}

object TestRepository {

  def apply(repository: Repository): TestRepository = new TestRepository(
    repository
  )

  def apply(workingDir: File): TestRepository = {
    if (!workingDir.exists()) {
      workingDir.mkdirs()
    }
    TestRepository(JGitAdapter.initRepository(workingDir, initialize = true))
  }

}
