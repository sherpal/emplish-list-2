package frontend.components.recipes

import models.emplishlist.Recipe
import models.emplishlist.db.DBRecipe
import org.scalajs.dom
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class RecipeRow extends StatelessComponent {

  case class Props(recipe: Either[DBRecipe, Recipe])

  case class RowInfo(recipeId: Int, recipeName: String, createdBy: String)

  def rowInfo: RowInfo = props.recipe match {
    case Left(recipe)  => RowInfo(recipe.uniqueId, recipe.name, recipe.createdBy)
    case Right(recipe) => RowInfo(recipe.uniqueId, recipe.name, recipe.createdBy)
  }

  def goToRecipe(recipeId: Int): Unit = {
    dom.document.location.href = Recipes.recipeViewPath(recipeId)
  }

  def render(): ReactElement =
    tr(
      td(className := "clickable", onClick := (() => goToRecipe(rowInfo.recipeId)))(rowInfo.recipeName),
      td(rowInfo.createdBy)
    )

}
