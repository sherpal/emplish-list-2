package frontend.components.ingredients

import frontend.NewItemPictogram
import frontend.utils.basket.BasketLoader
import models.emplishlist.{Ingredient, IngredientQuantity}
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react class IngredientRow extends StatelessComponent {

  case class Props(ingredient: Ingredient)

  def render(): ReactElement = tr(
    td(props.ingredient.name),
    td(props.ingredient.unit.name),
    td(
      span(
        className := "clickable",
        img(src := NewItemPictogram.asInstanceOf[String], alt := "add to basket", className := "icon-size"),
        onClick := (() => BasketLoader.addIngredient(IngredientQuantity(props.ingredient, 1)))
      )
    )
  )

}
