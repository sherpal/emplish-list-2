package frontend.laminar.components.main

import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import sttp.client._
import urldsl.language.PathSegment.dummyErrorImpl._

import scala.concurrent.ExecutionContext.Implicits.global
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.basket.BasketBoard
import frontend.laminar.components.headers.{GlobalHeader, Navigation}
import frontend.laminar.router.{Route, Router, Routes}
import models.users.User
import org.scalajs.dom.html

object MainBoard {

  def apply(): ReactiveHtmlElement[html.Div] = {

    def me = EventStream.fromFuture(
      boilerplate
        .get(path("me"))
        .response(responseAs[User])
        .send()
        .map(_.body)
        .map(_.toOption.flatMap(_.toOption))
    )

    def amIAdmin = EventStream.fromFuture(
      boilerplate
        .get(path("am-i-admin"))
        .response(ignore)
        .send()
        .map(_.is200)
    )

    val element = div(
      child <-- me
        .collect { case Some(user) => user }
        .combineWith(amIAdmin)
        .map {
          case (user, admin) =>
            div(
              className := "app",
              GlobalHeader(user),
              Navigation(admin),
              div(
                className := "main",
                div("Welcome to the main board"),
                children <-- Routes(
                  Route(root / "ingredients", () => div()), // todo
                  // new-ingredient route
                  // recipe route
                  // recipe editor container route
                  // recipe display container route
                  Route(root / "basket", () => BasketBoard())
                  // handle registration route
                )
              )
            )
        }
    )

    // go back to login if not connected
    me.filter(_.isEmpty).foreach(_ => Router.router.moveTo("/login"))(element)

    element
  }

}
