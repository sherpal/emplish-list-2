package frontend.laminar.router

import com.raquo.airstream.signal.Signal
import com.raquo.laminar.nodes.ReactiveElement
import org.scalajs.dom
import org.scalajs.dom.Element

object Routes {

  def apply(
      routes: List[Route[_ <: dom.Element, _]]
  ): Signal[List[ReactiveElement[Element]]] =
    Router.router.urlStream.map(url => routes.flatMap(_.render(url)))

  def apply(routes: Route[_ <: dom.Element, _]*): Signal[List[ReactiveElement[Element]]] = apply(routes.toList)

}
