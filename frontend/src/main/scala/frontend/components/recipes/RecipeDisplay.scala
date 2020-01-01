package frontend.components.recipes

import models.emplishlist.Recipe
import slinky.core.facade.ReactElement
import slinky.reactrouter.Link
import slinky.web.html._

object RecipeDisplay {

  def apply(recipe: Recipe): ReactElement = div(
    h1(s"${recipe.name} (by ${recipe.createdBy})"),
    section(
      s"(For ${recipe.forHowManyPeople} person${if (recipe.forHowManyPeople > 1) "s" else ""})"
    ),
    section(
      h2("Ingredients"),
      RecipeIngredientsDisplay(recipe.ingredients)
    ),
    section(
      h2("Description"),
      pre(recipe.description)
    ),
    section(
      Link(to = Recipes.editRecipePath(recipe.uniqueId))("Edit")
    )
  )

}
