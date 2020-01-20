package frontend.components.main

import router.Router
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.div

@react final class RedirectHome extends StatelessComponent {

  type Props = Unit

  override def componentWillMount(): Unit = {
    println("hello")
    Router.router.moveTo("/home")
  }

  def render(): ReactElement = div(
    "Redirecting..."
  )

}
