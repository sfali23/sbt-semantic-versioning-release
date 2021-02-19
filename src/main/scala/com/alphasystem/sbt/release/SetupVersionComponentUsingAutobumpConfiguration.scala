package com.alphasystem.sbt.release

class SetupVersionComponentUsingAutobumpConfiguration private (
  config: SemanticBuildVersionConfiguration) {

  private var _currentConfig = config
  private val autoBump = _currentConfig.autoBump

  // Keeps track of the highest-precedence autobump pattern that has been defined in the autobump configuration.
  // Since all patterns need not be specified, and any can be set to null, we need to figure out which pattern
  // corresponds to the highest-precedence version. For example, if only minorPattern and majorPattern are
  // non-null, then highestAutobumpPatternComponent is set to MAJOR.

  private var highestAutobumpPatternComponent = VersionComponent.NONE
  if (autoBump.majorPattern.nonEmpty) {
    highestAutobumpPatternComponent = VersionComponent.MAJOR
  } else if (autoBump.minorPattern.nonEmpty) {
    highestAutobumpPatternComponent = VersionComponent.MINOR
  } else if (autoBump.patchPattern.nonEmpty) {
    highestAutobumpPatternComponent = VersionComponent.PATCH
  }

  private var componentToAutobump = VersionComponent.NONE

  def configure(
    autobumpMessages: List[String]
  ): SemanticBuildVersionConfiguration = {
    if (!_currentConfig.isAutobumpEnabled) {
      return _currentConfig
    }

    // Checking commit messages and matching them against regexes is a costly process. So we will do our best to
    // get rid of unnecessary checks so that we can determine the appropriate component to bump very quickly. To do
    // this, we set up matchers for each of the corresponding version-components, that try to match the commit
    // message against the regex only if the current componentToAutobump is a component with lower precedence
    // than the corresponding component for the matcher.
    //
    // Whether the highestAutobumpPatternComponent is already bumped is checked separately and is not needed to be
    // checked inside the individual matchers.

    val patternMatchersMap = Map[VersionComponent, String => Boolean](
      // If majorPattern is defined, we set the corresponding matcher to check if the commit message matches
      // majorPattern
      VersionComponent.MAJOR -> ((message: String) => autoBump.major(message)),
      // If minorPattern is specified, we have two cases
      VersionComponent.MINOR -> ((message: String) =>
        minorPatternMatcher(message)
      ),
      VersionComponent.PATCH -> ((message: String) =>
        patchPatternMatcher(message)
      )
    )

    // sort from highest -> lowest, MAJOR, MINOR, Patch
    val patternMatchers =
      patternMatchersMap.toList.sortWith { (e1, e2) =>
        e1._1.compareTo(e2._1) > 0
      }

    import util.control.Breaks._

    for (message <- autobumpMessages) {
      breakable {
        if (
          !_currentConfig.newPreRelease &&
          autoBump.newPreReleasePattern.nonEmpty
        ) {
          _currentConfig = _currentConfig
            .copy(newPreRelease = autoBump.newPreRelease(message))
        }

        if (
          !_currentConfig.promoteToRelease &&
          autoBump.promoteToReleasePattern.nonEmpty
        ) {
          _currentConfig = _currentConfig
            .copy(promoteToRelease = autoBump.promoteToRelease(message))
        }

        // If manual bump is forced anyway, no commit message matching for bump components is necessary
        if (
          _currentConfig.forceBump && !_currentConfig.componentToBump.isNone
        ) {
          break
        }

        // If autobump is already set to the highest value for which a pattern exists, do not do any more matching.
        if (componentToAutobump == highestAutobumpPatternComponent) {
          break
        }

        componentToAutobump = patternMatchers
          .collectFirst {
            case (component, func) if func(message) => component
          }
          .getOrElse(VersionComponent.NONE)

      } // end of breakable
    } // end of for loop

    if (!componentToAutobump.isNone) {
      if (_currentConfig.componentToBump.isNone) {
        // If autobump is set and manual bump not, use autobump
        _currentConfig = _currentConfig
          .copy(componentToBump = componentToAutobump)
      } else if (_currentConfig.componentToBump < componentToAutobump) {
        // If autobump and manual bump are set, but manual bump is less than autobump without force bump, throw exception
        throw new IllegalArgumentException(
          """You are trying to manually bump a version component with lower precedence than the one specified by the 
            |commit message. If you are sure you want to do this, use "forceBump"."""
            .stripMargin
            .replaceNewLines
        )
      }
      // Manual bump is at least autobump, use manual bump
    }

    _currentConfig
  }

  def minorPatternMatcher(message: String): Boolean = {
    // If we are also checking whether there is a match against majorPattern, we set the corresponding matcher
    // to check if the commit message matches minorPattern, but only if we are not already bumping the minor
    // version (gets rid of unnecessary check).
    //
    // Otherwise, we just check to see if it matches minorPattern
    if (autoBump.minorPattern.nonEmpty) {
      if (highestAutobumpPatternComponent.isMajor)
        !componentToAutobump.isMinor && autoBump.minor(message)
      else autoBump.minor(message)
    } else false
  }

  def patchPatternMatcher(message: String): Boolean = {
    if (autoBump.patchPattern.nonEmpty) {
      val patch = autoBump.patch(message)
      highestAutobumpPatternComponent match {
        case VersionComponent.MAJOR if autoBump.minorPattern.nonEmpty =>
          !Seq(VersionComponent.MINOR, VersionComponent.PATCH).contains(
            componentToAutobump
          ) && patch

        case VersionComponent.MAJOR | VersionComponent.MINOR =>
          !componentToAutobump.isPatch && patch
        case _ => patch

      }
    } else false
  }
}

object SetupVersionComponentUsingAutobumpConfiguration {

  def apply(
    config: SemanticBuildVersionConfiguration,
    autobumpMessages: List[String]
  ): SemanticBuildVersionConfiguration =
    new SetupVersionComponentUsingAutobumpConfiguration(config).configure(
      autobumpMessages
    )
}
