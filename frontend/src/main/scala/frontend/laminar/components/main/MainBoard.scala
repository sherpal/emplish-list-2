package frontend.laminar.components.main

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.components.basket.BasketBoard
import frontend.laminar.components.headers.{GlobalHeader, Navigation}
import frontend.laminar.components.ingredients.{IngredientBoard, NewIngredient}
import frontend.laminar.components.recipes.{RecipeBoard, RecipeDisplayContainer, RecipeEditorContainer}
import frontend.laminar.router.{Route, Router, Routes}
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.users.User
import org.scalajs.dom
import sttp.client._
import urldsl.language.PathSegment.dummyErrorImpl._
import urldsl.language.QueryParameters.dummyErrorImpl.{param => qParam}
import frontend.laminar.components.users.AcceptUser
import frontend.utils.Recipes

import scala.concurrent.ExecutionContext.Implicits.global

final class MainBoard private () extends Component[dom.html.Div] {

  private def me = EventStream.fromFuture(
    boilerplate
      .get(path("me"))
      .response(responseAs[User])
      .send()
      .map(_.body)
      .map(_.toOption.flatMap(_.toOption))
  )

  private def amIAdmin = EventStream.fromFuture(
    boilerplate
      .get(path("am-i-admin"))
      .response(ignore)
      .send()
      .map(_.is200)
  )

  val element: ReactiveHtmlElement[dom.html.Div] = {

    val meAndAdmin = me
      .collect { case Some(user) => user }
      .combineWith(amIAdmin)

    val element = div(
      child <-- meAndAdmin
        .map {
          case (user, admin) =>
            div(
              className := "app",
              GlobalHeader(user),
              Navigation(admin),
              div(
                className := "main",
                children <-- Routes(
                  Route(root / "home" / endOfSegments, () => div(h1("Welcome to Emplish List!"))),
                  Route(root / "ingredients", () => IngredientBoard()),
                  Route(root / "new-ingredient", () => NewIngredient()),
                  Route(Recipes.topLevelPath / endOfSegments, () => RecipeBoard()),
                  Route(
                    Recipes.editorPath / (segment[Int] || "new"),
                    (recipeIdOrNew: Either[Int, Unit]) => RecipeEditorContainer(recipeIdOrNew.swap.toOption)
                  ),
                  Route(
                    Recipes.viewRecipePath / segment[Int],
                    (recipeId: Int) => RecipeDisplayContainer(recipeId)
                  ),
                  Route(root / "basket", () => BasketBoard()),
                  Route(
                    (root / "handle-registration").filter(_ => admin) ?
                      (qParam[String]("userName").? & qParam[String]("randomKey").?),
                    (_: Unit, matching: (Option[String], Option[String])) => AcceptUser(matching._1, matching._2)
                  )
                )
              )
            )
        },
      child <-- meAndAdmin.fold(true)((_, _) => false).map {
        if (_)
          div(
            className := "app",
            GlobalHeader(User("", Option(dom.window.sessionStorage.getItem("userName")).getOrElse("..."))),
            Navigation(false)
          )
        else emptyNode
      }
    )

    // go back to login if not connected
    me.filter(_.isEmpty).foreach(_ => Router.router.moveTo("/login"))(element)
    me.collect { case Some(user) => user }
      .foreach(user => dom.window.sessionStorage.setItem("userName", user.name))(element)

    element
  }

}

object MainBoard {

  def apply() = new MainBoard()

}
