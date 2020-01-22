package frontend.components.main

import frontend.components.basket.BasketBoard
import frontend.components.header.{GlobalHeader, Navigation}
import frontend.components.ingredients.{IngredientsBoard, NewIngredient}
import frontend.components.recipes.{RecipeDisplayContainer, RecipeEditorContainer, Recipes, RecipesBoard}
import frontend.components.users.AcceptUser
import frontend.utils.history.DefaultNavigator
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.users.User
import org.scalajs.dom
import org.scalajs.dom.History
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import sttp.client._
import urldsl.language.PathSegment.dummyErrorImpl._
import urldsl.language.QueryParameters.dummyErrorImpl.{param => qParam}
import urldsl.language.QueryParameters.dummyErrorImpl.{empty => noSearch}
import router._
import urldsl.vocabulary.UrlMatching

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

@react final class Main extends Component {

  type Props = Unit
  case class State(maybeUser: Option[User], amIAdmin: Boolean = false)

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

    boilerplate
      .get(path("am-i-admin"))
      .response(ignore)
      .send()
      .map(_.is200)
      .onComplete {
        case Success(true) => setState(_.copy(amIAdmin = true))
        case _             =>
      }
  }

  val history: History = DefaultNavigator.history

  def render(): ReactElement = state.maybeUser match {
    case Some(user) =>
      div(className := "app")(
        GlobalHeader(user),
        Navigation(state.amIAdmin),
        div(className := "main")(
          Routes(
            Router.router,
            List(
              Route((root / "ingredients") ? noSearch, () => IngredientsBoard()),
              Route((root / "new-ingredient") ? noSearch, () => NewIngredient()),
              Route((root / "recipes" / endOfSegments) ? noSearch, () => RecipesBoard()),
              Route(
                (Recipes.editorPath / (segment[Int] || "new")) ? noSearch,
                (matching: UrlMatching[Either[Int, Unit], Unit]) => RecipeEditorContainer(matching.path.swap.toOption)
              ),
              Route(
                (Recipes.viewRecipePath / segment[Int] / endOfSegments) ? noSearch,
                (matching: UrlMatching[Int, Unit]) => RecipeDisplayContainer(matching.path)
              ),
              Route((root / "basket") ? noSearch, () => BasketBoard()),
              Route(
                (root / "handle-registration").filter(_ => state.amIAdmin) ?
                  (qParam[String]("userName").? & qParam[String]("randomKey").?),
                (matching: UrlMatching[Unit, (Option[String], Option[String])]) =>
                  AcceptUser(matching.params._1, matching.params._2)
              )
            )
          )
        )
      )
    case _ =>
      div()
  }

}
