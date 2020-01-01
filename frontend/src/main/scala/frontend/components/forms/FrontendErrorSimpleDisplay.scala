package frontend.components.forms

import models.errors.BackendError
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.div

@react final class FrontendErrorSimpleDisplay extends StatelessComponent {

  type Props = BackendError

  def errorKey: String = props.errorKey
  def error: String = props.message

  def render(): ReactElement = div(errorKey, ": ", error)

}
