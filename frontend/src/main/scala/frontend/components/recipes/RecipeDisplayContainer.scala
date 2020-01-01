package frontend.components.recipes

import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.emplishlist.Recipe
import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.div

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

@react class RecipeDisplayContainer extends Component {
  type Props = Unit
  case class State(maybeRecipe: Option[Recipe])

  def initialState: State = State(None)

  override def componentWillMount(): Unit = {
    val id = dom.document.location.pathname.split("/").lastOption.flatMap(i => Try(i.toInt).toOption)

    boilerplate
      .response(responseAs[Option[Recipe]])
      .get(pathWithMultipleParams(Map("recipeId" -> List(id.getOrElse("-1").toString)), "recipes", "get"))
      .send()
      .map(_.body)
      .onComplete {
        case Success(Right(Right(Some(recipe)))) =>
          setState(_.copy(maybeRecipe = Some(recipe)))
        case _ =>
          dom.document.location.href = Recipes.topLevelPath
      }

  }

  def render(): ReactElement = state.maybeRecipe match {
    case Some(recipe) => RecipeDisplay(recipe)
    case None         => div()
  }
}
