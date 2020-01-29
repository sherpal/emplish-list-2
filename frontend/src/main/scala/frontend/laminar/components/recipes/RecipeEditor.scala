package frontend.laminar.components.recipes

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.utils.ActorSystemContainer
import models.emplishlist.{Ingredient, Recipe}
import org.scalajs.dom

final class RecipeEditor private (maybeRecipe: Option[Recipe], ingredients: Vector[Ingredient])(
    implicit actorSystemContainer: ActorSystemContainer
) extends Component[dom.html.Div] {
  val element: ReactiveHtmlElement[dom.html.Div] = div(
    maybeRecipe match {
      case Some(recipe) => h1(s"Edit: ${recipe.name} (by ${recipe.createdBy})")
      case None         => h1("New recipe")
    },
    RecipeForm(maybeRecipe.getOrElse(Recipe.empty), ingredients, maybeRecipe.isEmpty)
  )
}

object RecipeEditor {

  def apply(maybeRecipe: Option[Recipe], ingredients: Vector[Ingredient])(
      implicit actorSystemContainer: ActorSystemContainer
  ): RecipeEditor = new RecipeEditor(maybeRecipe, ingredients)

}
