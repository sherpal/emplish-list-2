package frontend.forms

import java.util.UUID

import slinky.core.facade.ReactElement
import slinky.web.html._

final case class FormError(errorKey: String, error: String) {
  def display: ReactElement = div(key := UUID.randomUUID().toString)(errorKey, ": ", error)
}
