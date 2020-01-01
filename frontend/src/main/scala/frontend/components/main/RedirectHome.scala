package frontend.components.main

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.reactrouter.Redirect

@react final class RedirectHome extends StatelessComponent {

  type Props = Unit

  def render(): ReactElement = Redirect(to = "/home")

}
