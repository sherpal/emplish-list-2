package frontend.components.recipes

import models.RecipeSummary
import router.Router
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class RecipeRow extends StatelessComponent {

  case class Props(recipe: RecipeSummary)

  def recipe: RecipeSummary = props.recipe

  case class RowInfo(recipeId: Int, recipeName: String, createdBy: String)

  def rowInfo: RowInfo = RowInfo(recipe.uniqueId, recipe.name, recipe.createdBy)

  def goToRecipe(recipeId: Int): Unit = {
    Router.router.moveTo(Recipes.recipeViewPath(recipeId).createPath())
//    dom.document.location.href = Recipes.recipeViewPath(recipeId)
  }

  def render(): ReactElement =
    tr(
      td(className := "clickable", onClick := (() => goToRecipe(rowInfo.recipeId)))(rowInfo.recipeName),
      td(rowInfo.createdBy)
    )

}
