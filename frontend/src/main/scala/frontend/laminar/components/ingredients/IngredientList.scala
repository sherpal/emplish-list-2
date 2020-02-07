package frontend.laminar.components.ingredients

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.NewItemPictogram
import frontend.laminar.components.forms.Tags
import frontend.laminar.components.helpers.InputTags
import frontend.laminar.router.{Link, Router}
import frontend.utils.basket.BasketLoader
import models.emplishlist.{Ingredient, IngredientQuantity}
import org.scalajs.dom.html
import urldsl.language.PathSegment.dummyErrorImpl._

private[ingredients] object IngredientList {

  private def row(ingredient: Ingredient) = tr(
    td(
      className := "clickable",
      onClick.mapTo(ingredient.id) --> (
          id =>
            Router.router.moveTo(
              "/" + (root / "update-ingredient" / segment[Int]).createPath(id)
            )
        ),
      ingredient.name
    ),
    td(ingredient.unit.name),
    td(className := "ellipsis-td", title := ingredient.tags.mkString(", "), ingredient.tags.mkString(", ")),
    td(
      span(
        title := "Add to basket",
        className := "clickable",
        img(src := NewItemPictogram.asInstanceOf[String], alt := "add to basket", className := "icon-size"),
        onClick --> (_ => BasketLoader.addIngredient(IngredientQuantity(ingredient, 1)))
      )
    )
  )

  def apply(ingredients: Vector[Ingredient]): ReactiveHtmlElement[html.Element] = {

    val tagsFilter = new EventBus[List[String]]()
    val $tags = tagsFilter.events.startWith(Nil)
    val $rows = $tags.map { tags =>
      ingredients.filter(ingredient => tags.forall(ingredient.tags.contains))
    }.map(_.take(100)).map(_.map(row))

    section(
      h1("Ingredients in database"),
      section(
        Tags.tagsFilterExplanation("ingredient"),
        p(
          InputTags($tags, tagsFilter.writer)
        )
      ),
      span(className := "clickable", Link(to = root / "new-ingredient")("New Ingredient")),
      table(
        thead(
          th("Name"),
          th("Unit"),
          th("tags")
        ),
        tbody(
          children <-- $rows
        )
      )
    )
  }

}
