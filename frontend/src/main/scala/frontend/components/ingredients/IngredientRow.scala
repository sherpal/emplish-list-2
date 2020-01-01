package frontend.components.ingredients

import models.emplishlist.Ingredient
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react class IngredientRow extends StatelessComponent {

  case class Props(ingredient: Ingredient)

  def render(): ReactElement = tr(
    td(props.ingredient.name),
    td(props.ingredient.unit.name)
  )

}
