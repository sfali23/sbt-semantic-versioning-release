package com.alphasystem.sbt

package object release {

  implicit def ordered: Ordering[Version] =
    (x: Version, y: Version) =>
      new VersionComparator().compare(x.value, y.value)

  implicit class StringOps(src: String) {
    def replaceNewLines: String = src.replaceAll(System.lineSeparator(), "")
  }

  implicit class VersionComponentOps(src: VersionComponent) {
    def <(other: VersionComponent): Boolean = src.ordinal() < other.ordinal()
  }

}
