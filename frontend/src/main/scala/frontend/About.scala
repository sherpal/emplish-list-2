package frontend

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.h1

@react final class About extends StatelessComponent {

  type Props = Unit

  def render(): ReactElement = h1("About")

}
