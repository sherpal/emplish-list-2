package frontend.components.header

import frontend.components.helpers.Logout
import models.users.User
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class GlobalHeader extends StatelessComponent {

  case class Props(user: User)

  def render(): ReactElement = header(
    span("Emplish List"),
    span(props.user.name),
    Logout()
  )

}
