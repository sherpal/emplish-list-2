package frontend.laminar.components

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.AppCSS
import frontend.laminar.components.helpers.Redirect
import frontend.laminar.components.login.{Login, Register}
import frontend.laminar.components.main.MainBoard
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
    "handle-registration"
  )

  def apply(): ReactiveHtmlElement[html.Div] = div(
    child <-- Routes
      .firstOf(
        Route(r / endOfSegments, () => Redirect(r / "home")),
        Route(mainBoardRoutes, () => MainBoard()),
        Route(r / "login", () => Login()),
        Route(r / "sign-up", () => Register())
      )
      .map {
        case Some(elem) => elem
        case None       => div("huh?!")
      }
  )

}
