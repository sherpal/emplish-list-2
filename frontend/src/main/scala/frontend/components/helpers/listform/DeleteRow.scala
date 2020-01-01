package frontend.components.helpers.listform

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class DeleteRow extends StatelessComponent {

  case class Props(delete: () => Unit)

  def render(): ReactElement = span(className := "clickable red", onClick := props.delete)("-")

}
