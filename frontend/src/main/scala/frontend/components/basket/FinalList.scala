package frontend.components.basket

import frontend.components.recipes.RecipeIngredientsDisplay
import models.emplishlist.IngredientQuantity
import slinky.core.facade.ReactElement
import slinky.web.html._

object FinalList {

  def apply(ingredients: List[IngredientQuantity], backToMakeList: () => Unit): ReactElement = section(
    h2("Basket list"),
    Some("There are no ingredients in your basket.").filter(_ => ingredients.isEmpty),
    RecipeIngredientsDisplay(ingredients),
    p(
      button(onClick := backToMakeList, "Back to basket selection")
    )
  )

}
