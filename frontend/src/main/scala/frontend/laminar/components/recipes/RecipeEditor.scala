package frontend.laminar.components.recipes

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.utils.ActorSystemContainer
import models.emplishlist.{Ingredient, Recipe}
import org.scalajs.dom
import org.scalajs.dom.html.Form

final class RecipeEditor private (maybeRecipe: Option[Recipe], ingredients: Vector[Ingredient])(
    implicit actorSystemContainer: ActorSystemContainer
) extends Component[dom.html.Form] {
  val element: ReactiveHtmlElement[Form] = form(
    "This is the recipe editor"
  )
}

object RecipeEditor {

  def apply(maybeRecipe: Option[Recipe], ingredients: Vector[Ingredient])(
      implicit actorSystemContainer: ActorSystemContainer
  ): RecipeEditor = new RecipeEditor(maybeRecipe, ingredients)

}
