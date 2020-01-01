package frontend.components.ingredients

import models.emplishlist.IngredientUnit
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class UnitList extends StatelessComponent {

  case class Props(units: Vector[IngredientUnit])

  def render(): ReactElement = section(
    h1("List of units"),
    ul(
      props.units.map(u => li(key := u.name, u.name))
    )
  )

}
