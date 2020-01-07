package frontend.components.helpers.forms

import slinky.core.facade.ReactElement
import slinky.web.html._

object InputTextArea {

  def apply(
      title: String,
      v: String,
      updater: String => Unit
  ): ReactElement =
    p(
      title,
      br(),
      textarea(
        value := v,
        onChange := (event => updater(event.target.value))
      )
    )

}
