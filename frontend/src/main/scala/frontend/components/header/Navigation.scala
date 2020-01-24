package frontend.components.header

import router.Link
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class Navigation extends StatelessComponent {

  type Props = Boolean

  def render(): ReactElement = nav(
    ul(
      li(Link(to = "/home", text = "Home")),
      li(Link(to = "/ingredients", text = "Ingredients")),
      li(Link(to = "/recipes", text = "Recipes")),
      li(Link(to = "/basket", text = "Create list")),
      Some(li(Link(to = "/handle-registration", text = "Registrations"))).filter(_ => props)
    )
  )

}
