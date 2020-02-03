package frontend.laminar.components.recipes

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.router.Router
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import io.circe.generic.auto._
import models.emplishlist.Recipe
import org.scalajs.dom
import org.scalajs.dom.html.Div
import urldsl.language.QueryParameters.dummyErrorImpl.{param => qParam}

final class RecipeDisplayContainer(recipeId: Int)(implicit actorSystemContainer: ActorSystemContainer)
    extends Component[dom.html.Div] {

  import actorSystemContainer._

  val element: ReactiveHtmlElement[Div] = {
    val $recipe =
      new InfoDownloader("recipes").downloadInfoWithParams[Recipe, Int]("get", qParam[Int]("recipeId"))(recipeId)

    implicit val elem: ReactiveHtmlElement[Div] = div(
      child <-- $recipe.collect { case Some(recipe) => recipe }.map(RecipeDisplay.apply)
    )

    $recipe.filter(_.isEmpty).foreach { _ =>
      Router.router.moveTo("/home")
    }

    elem
  }
}

object RecipeDisplayContainer {

  def apply(recipeId: Int)(implicit actorSystemContainer: ActorSystemContainer): RecipeDisplayContainer =
    new RecipeDisplayContainer(recipeId)

}
