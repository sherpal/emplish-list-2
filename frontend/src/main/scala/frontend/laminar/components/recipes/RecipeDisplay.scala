package frontend.laminar.components.recipes

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.router.Link
import frontend.utils.Recipes
import models.emplishlist.Recipe
import org.scalajs.dom
import org.scalajs.dom.html.Div

final class RecipeDisplay(recipe: Recipe) extends Component[dom.html.Div] {
  val element: ReactiveHtmlElement[Div] = div(
    h1(s"${recipe.name} (by ${recipe.createdBy})"),
    section(
      s"(For ${recipe.forHowManyPeople} person${if (recipe.forHowManyPeople > 1) "s" else ""})"
    ),
    section(
      h2("Ingredients"),
      RecipeIngredientsDisplay(Val(recipe.ingredients))
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

object RecipeDisplay {
  def apply(recipe: Recipe): RecipeDisplay = new RecipeDisplay(recipe)
}
