package frontend.components.main

import org.scalajs.dom
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{div, h1}

@react final class Todo extends StatelessComponent {

  type Props = Unit

  override def componentDidMount(): Unit = {
    dom.window.location.href = "/todo"
  }

  def render(): ReactElement = div(h1("TODO"))

}
