package router

import org.scalajs.dom
import org.scalajs.dom.raw.PopStateEvent

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.timers.setTimeout
import scala.concurrent.duration._

final class Router private () {

  def url: String = dom.window.location.href

  private val subscriptions: mutable.Map[String, (String, js.Any) => Unit] = mutable.Map()

  private def trigger(stateData: js.Any): Unit = {
    subscriptions.valuesIterator.foreach(_.apply(url, stateData))
  }

  dom.window.addEventListener("popstate", (event: PopStateEvent) => {
    trigger(event.state)
  })

  def subscribe(name: String, handler: (String, js.Any) => Unit): Unit = {
    subscriptions += name -> handler
  }

  def unsubscribe(name: String): Unit = {
    subscriptions -= name
  }

  def moveTo(url: String, stateData: js.Any = null): Unit = {
    dom.window.history.pushState(stateData, "Title", url) // todo: set the title?
    setTimeout(1.millisecond) {
      trigger(stateData)
    }
  }

}

object Router {

  final lazy val router: Router = new Router

}
