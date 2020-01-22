package frontend.components.recipes

import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.emplishlist.Recipe
import org.scalajs.dom
import router.Router
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.div

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

@react class RecipeDisplayContainer extends Component {
  case class Props(recipeId: Int)
  case class State(maybeRecipe: Option[Recipe])

  def initialState: State = State(None)

  override def componentWillMount(): Unit = {
    boilerplate
      .response(responseAs[Option[Recipe]])
      .get(pathWithMultipleParams(Map("recipeId" -> List(props.recipeId.toString)), "recipes", "get"))
      .send()
      .map(_.body)
      .onComplete {
        case Success(Right(Right(Some(recipe)))) =>
          setState(_.copy(maybeRecipe = Some(recipe)))
        case _ =>
          Router.router.moveTo("/" + Recipes.topLevelPath.createPath())
      }

  }

  def render(): ReactElement = state.maybeRecipe match {
    case Some(recipe) => RecipeDisplay(recipe)
    case None         => div()
  }
}
