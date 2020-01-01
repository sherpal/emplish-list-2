package frontend.components.helpers

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class SimpleButton extends StatelessComponent {

  case class Props(label: String, onClick: () => Unit)

  def render(): ReactElement = button(onClick := props.onClick)(props.label)

}
