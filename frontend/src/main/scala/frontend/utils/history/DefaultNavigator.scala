package frontend.utils.history

import frontend.utils.history.Navigator.Url
import org.scalajs.dom

import scala.scalajs.js

object DefaultNavigator extends Navigator {

  val history: dom.History = History.createBrowserHistory()

  def pushState(stateData: js.Any, title: String, maybeUrl: Option[Url]): Unit = maybeUrl match {
    case Some(url) => history.pushState(stateData, title, url)
    case None      => history.pushState(stateData, title)
  }

}
