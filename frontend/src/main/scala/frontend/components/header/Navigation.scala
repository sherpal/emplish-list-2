package frontend.components.header

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.reactrouter.Link
import slinky.web.html._

@react final class Navigation extends StatelessComponent {

  type Props = Unit

  def render(): ReactElement = nav(
    ul(
      li(Link(to = "/home")("Home")),
      li(Link(to = "/ingredients")("Ingredients")),
      li(Link(to = "/recipes")("Recipes")),
      li(Link(to = "/basket")("Create list"))
    )
  )

}
