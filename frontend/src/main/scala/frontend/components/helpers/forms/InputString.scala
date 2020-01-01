package frontend.components.helpers.forms

import akka.stream.QueueOfferResult
import frontend.components.forms.FrontendErrorSimpleDisplay
import models.errors.BackendError
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.concurrent.Future

@react final class InputString extends StatelessComponent {

  case class Props(
      value: String,
      title: String,
      changeFormData: String => Future[QueueOfferResult],
      errors: List[BackendError]
  )

  def render(): ReactElement =
    div(
      className := "input-string"
    )(
      props.title,
      " ",
      input(onChange := (event => props.changeFormData(event.target.value)), value := props.value),
      props.errors.map(FrontendErrorSimpleDisplay(_).withKey(java.util.UUID.randomUUID().toString))
    )

}
