package frontend.components.main

import frontend.components.basket.BasketBoard
import frontend.components.header.{GlobalHeader, Navigation}
import frontend.components.ingredients.{IngredientsBoard, NewIngredient}
import frontend.components.recipes.{RecipeDisplayContainer, RecipeEditorContainer, Recipes, RecipesBoard}
import frontend.utils.history.DefaultNavigator
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.users.User
import org.scalajs.dom
import org.scalajs.dom.History
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.reactrouter._
import slinky.web.html._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

@react final class Main extends Component {

  type Props = Unit
  case class State(maybeUser: Option[User])

  override def initialState: State = State(None)

  def me(): Unit =
    boilerplate
      .get(path("me"))
      .response(responseAs[User])
      .send()
      .map(_.body)
      .onComplete {
        case Success(Right(Right(user))) =>
          setState(_.copy(maybeUser = Some(user)))
        case _ =>
          dom.window.location.href = "/login"
      }

  override def componentWillMount(): Unit = {
    me()
  }

  val history: History = DefaultNavigator.history

  def render(): ReactElement = state.maybeUser match {
    case Some(user) =>
      div(className := "app")(
        GlobalHeader(user),
        Navigation(),
        div(className := "main")(
          Router(history)(
            Route(path = "/ingredients", component = IngredientsBoard),
            Route(path = "/new-ingredient", component = NewIngredient),
            Route(path = "/recipes", component = RecipesBoard, exact = true),
            Route(path = Recipes.editorPath + ":id", component = RecipeEditorContainer),
            Route(path = Recipes.viewRecipePath + ":id", component = RecipeDisplayContainer, exact = true),
            Route(path = "/basket", component = BasketBoard)
          )
        )
      )
    case _ =>
      div()
  }

}
