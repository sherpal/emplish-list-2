package frontend.laminar.components

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.AppCSS
import frontend.laminar.components.helpers.Redirect
import frontend.laminar.components.login.{AfterRegister, Login, Register}
import frontend.laminar.components.main.MainBoard
import frontend.laminar.fixlaminar.Fixes
import frontend.laminar.router.{Route, Routes}
import org.scalajs.dom.html
import urldsl.language.PathSegment.dummyErrorImpl._

object App {

  private val css = AppCSS
  println(css)

  private def r = root

  private val mainBoardRoutes = r / oneOf(
    "home",
    "ingredients",
    "new-ingredient",
    "recipes",
    "basket",
    "handle-registration",
    "view-users"
  )

  def apply(): ReactiveHtmlElement[html.Div] = div(
    Fixes.readMountEvents,
    child <-- Routes
      .firstOf(
        Route(r / endOfSegments, () => Redirect(r / "home")),
        Route(mainBoardRoutes, () => MainBoard()),
        Route(r / "login", () => Login()),
        Route(r / "sign-up", () => Register()),
        Route(r / "after-registration", () => AfterRegister())
      )
      .map {
        case Some(elem) => elem
        case None       => div("huh?!")
      }
  )

}
