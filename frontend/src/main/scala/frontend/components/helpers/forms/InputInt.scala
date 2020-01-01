package frontend.components.helpers.forms

import slinky.core.facade.ReactElement
import slinky.web.html._

object InputInt {

  def apply(title: String, currentValue: Int, updater: Int => Unit): ReactElement = span(
    title,
    " ",
    input(
      value := currentValue.toString,
      onChange := (event => updater(event.target.valueAsNumber.toInt)),
      `type` := "number"
    )
  )

}
