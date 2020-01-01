package frontend.components.ingredients

import models.emplishlist.Store
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class StoreList extends StatelessComponent {

  case class Props(stores: Vector[Store])

  def render(): ReactElement = section(
    h1("List of stores"),
    ul(
      props.stores.map(s => li(key := s.name, s.name))
    )
  )

}
