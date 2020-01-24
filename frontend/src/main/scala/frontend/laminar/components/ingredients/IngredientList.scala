package frontend.laminar.components.ingredients

import models.emplishlist.{Ingredient, IngredientQuantity}
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.NewItemPictogram
import frontend.laminar.router.Link
import frontend.utils.basket.BasketLoader
import org.scalajs.dom.html
import urldsl.language.PathSegment.dummyErrorImpl._

private[ingredients] object IngredientList {

  private def row(ingredient: Ingredient) = tr(
    td(ingredient.name),
    td(ingredient.unit.name),
    td(
      span(
        className := "clickable",
        img(src := NewItemPictogram.asInstanceOf[String], alt := "add to basket", className := "icon-size"),
        onClick --> (_ => BasketLoader.addIngredient(IngredientQuantity(ingredient, 1)))
      )
    )
  )

  def apply(ingredients: Vector[Ingredient]): ReactiveHtmlElement[html.Element] = {

    section(
      h1("Ingredients in database"),
      span(className := "clickable", Link(to = root / "new-ingredient")("New Ingredient")),
      table(
        thead(
          th("Name"),
          th("Unit")
        ),
        tbody(
          ingredients.map(row)
        )
      )
    )
  }

}
