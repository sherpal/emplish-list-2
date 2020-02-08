package frontend.laminar.components.headers

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.router.Link
import frontend.utils.Recipes
import org.scalajs.dom.html
import urldsl.language.PathSegment.dummyErrorImpl._

import frontend.laminar.components.App.RouteDefinitions._

object Navigation {

  def apply(amIAdmin: Boolean): ReactiveHtmlElement[html.Element] = nav(
    ul(
      li(Link(mainBoardRoutes)("Home")),
      li(Link(root / "ingredients")("Ingredients")),
      li(Link(Recipes.topLevelPath)("Recipes")),
      li(Link(root / "basket")("Create list")),
      Some(li(Link(root / "handle-registration")("Registration"))).filter(_ => amIAdmin),
      Some(li(Link(root / "view-users")("Users"))).filter(_ => amIAdmin),
      Some(li(Link(testRoute)("Test"))).filter(_ => scala.scalajs.LinkingInfo.developmentMode)
    )
  )

}
