package frontend.utils.history

import frontend.utils.history.Navigator.Url
import org.scalajs.dom

import scala.scalajs.js

trait Navigator {

  val history: dom.History

  def pushState(stateData: js.Any, title: String, maybeUrl: Option[Url]): Unit

  final def pushState(stateData: js.Any, title: String): Unit = pushState(stateData, title, None)

  final def pushState(stateData: js.Any, title: String, url: Url): Unit = pushState(stateData, title, Some(url))

}

object Navigator {

  type Url = String

}
