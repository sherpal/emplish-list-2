package frontend.components.helpers.lab

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.span

@react final class DummyComponent extends StatelessComponent {

  case class Props(content: String)

  def render(): ReactElement = span("Dummy: " + props.content)

}
