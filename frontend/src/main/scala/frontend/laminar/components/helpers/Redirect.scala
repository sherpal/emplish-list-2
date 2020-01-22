package frontend.laminar.components.helpers

import com.raquo.laminar.api.L._
import com.raquo.laminar.lifecycle.NodeDidMount
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.router.Router
import org.scalajs.dom.html
import urldsl.language.PathSegment

object Redirect {

  def apply[T](pathSegment: PathSegment[T, _], t: T): ReactiveHtmlElement[html.Div] = {
    val elem = div("Redirecting")
    elem.subscribe(_.mountEvents) {
      case NodeDidMount =>
        Router.router.moveTo(pathSegment.createPath(t))
      case _ => // do nothing
    }
    elem
  }

  def apply(pathSegment: PathSegment[Unit, _]): ReactiveHtmlElement[html.Div] = apply[Unit](pathSegment, ())

}
