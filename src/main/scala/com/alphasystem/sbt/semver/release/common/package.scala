package com.alphasystem
package sbt
package semver
package release

import org.eclipse.jgit.lib.{ObjectId, Ref}

package object common {

  implicit class RefOps(src: Ref) {
    def getNonNullObjectId: ObjectId = Option(src.getPeeledObjectId).getOrElse(src.getObjectId)
  }
}
