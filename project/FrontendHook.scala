import play.sbt.PlayRunHook
import sbt._

import scala.sys.process.Process

/**
 * Frontend build play run hook.
 * https://www.playframework.com/documentation/2.7.x/SBTCookbook
 */
object FrontendHook {
  def apply(base: File): PlayRunHook = {
    object UIBuildHook extends PlayRunHook {

      var process: Option[Process] = None

      /**
       * Executed before play run start.
       * Run npm install if node modules are not installed.
       */
      override def beforeStarted(): Unit = {

      }

      /**
       * Executed after play run start.
       * Run npm start
       */
      override def afterStarted(): Unit = { // todo: fix me!
        process = Option(
          Process("sbt dev").run
        )
      }

      /**
       * Executed after play run stop.
       * Cleanup frontend execution processes.
       */
      override def afterStopped(): Unit = {
        process.foreach(_.destroy())
        process = None
      }

    }

    UIBuildHook
  }
}
