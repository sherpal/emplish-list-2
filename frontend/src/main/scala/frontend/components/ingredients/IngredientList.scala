package frontend.components.ingredients

import models.emplishlist.Ingredient
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.reactrouter.Link
import slinky.web.html._

@react final class IngredientList extends Component {

  case class Props(ingredients: Vector[Ingredient])

  case class State(filters: Map[String, String]) // todo
  def initialState: State = State(Map())

  def ingredients: Vector[Ingredient] = props.ingredients.filter(_ => true) // todo: filter that with state filters

  def render(): ReactElement = section(
    h1("Ingredients in database"),
    span(className := "clickable")(Link(to = "/new-ingredient")("New Ingredient")),
    table(
      thead(
        tr(
          th("Name"),
          th("Unit")
        )
      ),
      tbody(
        ingredients.map(ingredient => IngredientRow(ingredient).withKey(ingredient.name))
      )
    )
  )

}
