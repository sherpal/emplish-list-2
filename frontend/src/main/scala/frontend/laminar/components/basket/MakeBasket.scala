package frontend.laminar.components.basket

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import models.emplishlist.{Ingredient, Recipe}
import org.scalajs.dom.html

object MakeBasket {

  def apply(recipes: List[Recipe], ingredients: List[Ingredient]): ReactiveHtmlElement[html.Div] =
    div("This will allow to make the basket")

}
