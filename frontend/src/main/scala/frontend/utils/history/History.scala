package frontend.utils.history

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("history", JSImport.Default)
@js.native
object History extends js.Object {
  def createBrowserHistory(): dom.History = js.native
}
