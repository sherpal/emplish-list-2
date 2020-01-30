package frontend.laminar.components.recipes

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveElement
import models.emplishlist.IngredientQuantity
import org.scalajs.dom.html.UList

object RecipeIngredientsDisplay {

  def apply(ingredientQuantities: Observable[List[IngredientQuantity]]): ReactiveElement[UList] = ul(
    children <-- ingredientQuantities.map(_.map {
      case IngredientQuantity(ingredient, amount) =>
        li(s"${ingredient.name} ($amount ${ingredient.unit.name})")
    })
  )

}
