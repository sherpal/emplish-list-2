package frontend.laminar.components.basket

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.recipes.RecipeIngredientsDisplay
import models.emplishlist.IngredientQuantity
import org.scalajs.dom.html

object FinalList {

  def apply(
      ingredients: Observable[List[IngredientQuantity]],
      finish: Observer[Boolean]
  ): ReactiveHtmlElement[html.Element] = section(
    h2("Basket list"),
    p(
      child <-- ingredients.map(_.isEmpty).map(if (_) "There are no ingredients in your basket." else "")
    ),
    RecipeIngredientsDisplay(ingredients),
    p(
      button(onClick.mapTo(false) --> finish, "Bask to basket selection")
    )
  )

}
