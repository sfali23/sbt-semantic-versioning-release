package com.alphasystem
package sbt
package semver
package release
package internal

import com.alphasystem.sbt.semver.release.common.{JGitAdapter, TestRepository}
import org.scalatest.Assertion

import java.io.File
import java.util.UUID
import scala.reflect.io.Directory
import scala.util.{Failure, Success, Try}

trait TestSpec {
  protected val workingDir: File = new File(s"/tmp/${UUID.randomUUID()}")
  protected val testRepository: TestRepository = TestRepository(workingDir)
  protected val gitAdapter: JGitAdapter = JGitAdapter(workingDir)

  protected def populateRepository(): Unit = ()

  protected def assertion: Assertion

  private def runTest(): Unit = {
    populateRepository()
    Try(assertion) match {
      case Failure(ex) =>
        new Directory(workingDir).deleteRecursively()
        throw ex
      case Success(_) =>
        new Directory(workingDir).deleteRecursively()
    }
  }

  runTest()
}
