package frontend.components.helpers.listform

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class AddRow extends StatelessComponent {

  case class Props(add: () => Unit)

  def render(): ReactElement = span(className := "clickable green", onClick := props.add)("+")

}
