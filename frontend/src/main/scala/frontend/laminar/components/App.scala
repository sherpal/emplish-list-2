package frontend.laminar.components

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.AppCSS
import frontend.laminar.components.helpers.Redirect
import frontend.laminar.components.login.{AfterRegister, Login, Register}
import frontend.laminar.components.main.MainBoard
import frontend.laminar.components.tests.TestComponent
import frontend.laminar.fixlaminar.Fixes
import frontend.laminar.router.{Route, Routes}
import org.scalajs.dom.html
import urldsl.language.PathSegment.dummyErrorImpl._

object App {

  private val css = AppCSS
  println(css)

  private def r = root

  object RouteDefinitions {
    final val mainBoardRoutes = r / oneOf(
      "home",
      "ingredients",
      "new-ingredient",
      "update-ingredient",
      "recipes",
      "basket",
      "handle-registration",
      "view-users"
    )

    final val loginRoute = r / "login"
    final val signUpRoute = r / "sign-up"
    final val afterRegisterRoute = r / "after-registration"
    final val testRoute = (r / "test").filter(_ => scala.scalajs.LinkingInfo.developmentMode)
  }

  import RouteDefinitions._

  def apply(): ReactiveHtmlElement[html.Div] = div(
    Fixes.readMountEvents,
    child <-- Routes
      .firstOf(
        Route(r / endOfSegments, () => Redirect(mainBoardRoutes)),
        Route(mainBoardRoutes, () => MainBoard()),
        Route(loginRoute, () => Login()),
        Route(signUpRoute, () => Register()),
        Route(afterRegisterRoute, () => AfterRegister()),
        Route(testRoute, () => TestComponent())
      )
      .map {
        case Some(elem) => elem
        case None       => div("huh?!")
      }
  )

}
