package router

import slinky.core.facade.ReactElement
import urldsl.language.{PathSegment, PathSegmentWithQueryParams}
import urldsl.vocabulary.UrlMatching

final class Route[P, Q] private (
    pathSegment: PathSegmentWithQueryParams[P, _, Q, _],
    display: UrlMatching[P, Q] => ReactElement
) {

  def render(url: String): Option[ReactElement] =
    pathSegment.matchRawUrl(url).toOption.map(display)

}

object Route {

  def apply[P, Q](
      pathSegment: PathSegmentWithQueryParams[P, _, Q, _],
      display: UrlMatching[P, Q] => ReactElement
  ): Route[P, Q] =
    new Route(pathSegment, display)

  def apply(
      pathSegment: PathSegmentWithQueryParams[Unit, _, Unit, _],
      display: () => ReactElement
  ): Route[Unit, Unit] =
    new Route(pathSegment, (_: UrlMatching[Unit, Unit]) => display())

}
