package frontend.laminar.router

import com.raquo.laminar.nodes.ReactiveElement
import frontend.laminar.router.Router.Url
import org.scalajs.dom
import urldsl.language.PathSegment

final class Route[Ref <: dom.Element, T] private (
    val matcher: Url => Option[T],
    renderer: T => ReactiveElement[Ref]
) {

  def render(url: Url): Option[ReactiveElement[Ref]] = {
    matcher(url).map(renderer)
  }

  def maybeMakeRenderer(url: Url): Option[() => ReactiveElement[Ref]] = matcher(url).map(t => () => renderer(t))

}

object Route {

  def apply[T, Ref <: dom.Element](
      pathSegment: PathSegment[T, _],
      renderer: T => ReactiveElement[Ref]
  ): Route[Ref, T] =
    new Route((url: Url) => pathSegment.matchRawUrl(url).toOption, renderer)

  def apply[Ref <: dom.Element](
      pathSegment: PathSegment[Unit, _],
      renderer: () => ReactiveElement[Ref]
  ): Route[Ref, Unit] = apply(pathSegment, (_: Unit) => renderer())

}
