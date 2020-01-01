package frontend.components.recipes

import models.emplishlist.IngredientQuantity
import slinky.core.facade.ReactElement
import slinky.web.html._

object RecipeIngredientsDisplay {

  def apply(ingredientQuantities: List[IngredientQuantity]): ReactElement = ul(
    ingredientQuantities.map {
      case IngredientQuantity(ingredient, amount) =>
        li(key := ingredient.name)(
          s"${ingredient.name} ($amount ${ingredient.unit.name})"
        )
    }
  )

}
