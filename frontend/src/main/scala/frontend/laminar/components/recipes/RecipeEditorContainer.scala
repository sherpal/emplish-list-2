package frontend.laminar.components.recipes

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import io.circe.generic.auto._
import models.emplishlist.{Ingredient, Recipe}
import org.scalajs.dom
import org.scalajs.dom.html.Div
import urldsl.language.QueryParameters.dummyErrorImpl.{param => qParam}

/**
  * Component for editing or creating recipes.
  * @param recipeIdOrNew None for a new recipe, Some(recipeId) for editing recipe with the given Id.
  */
final class RecipeEditorContainer(recipeIdOrNew: Option[Int])(implicit actorSystemContainer: ActorSystemContainer)
    extends Component[dom.html.Div] {

  import actorSystemContainer._

  val ingredientDownloader = new InfoDownloader("ingredients")
  val recipeDownloader = new InfoDownloader("recipes")

  val element: ReactiveHtmlElement[Div] = {

    /** Fetching -1 for a new so that the server responds with "None". */
    val $maybeRecipe =
      recipeDownloader
        .downloadInfoWithParams[Option[Recipe], Int]("get", qParam[Int]("recipeId"))(recipeIdOrNew.getOrElse(-1))
        .map(_.flatten) // need to flatten because of possible failures

    val $maybeRecipeAndIngredients =
      ingredientDownloader
        .downloadInfo[Vector[Ingredient]]("ingredients")
        .collect { case Some(ingredients) => ingredients }
        .combineWith($maybeRecipe)

    div(
      child <-- $maybeRecipeAndIngredients.map(_.swap).map((RecipeEditor.apply _).tupled).map(identity[RecipeEditor]),
      child <-- $maybeRecipeAndIngredients.fold(div("Loading"))((_, _) => div())
    )
  }
}

object RecipeEditorContainer {
  def apply(recipeIdOrNew: Option[Int])(implicit actorSystemContainer: ActorSystemContainer): RecipeEditorContainer =
    new RecipeEditorContainer(recipeIdOrNew)
}
