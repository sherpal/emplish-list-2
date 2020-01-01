package frontend.components.login

import akka.stream.QueueOfferResult
import models.errors.BackendError
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.concurrent.Future

@react final class InputPassword extends StatelessComponent {

  case class Props(
      title: String,
      changeFormData: String => Future[QueueOfferResult],
      errors: Map[String, List[BackendError]]
  )

  def render(): ReactElement =
    div(
      className := "input-string"
    )(
      props.title,
      input(`type` := "password", onChange := (event => props.changeFormData(event.target.value))),
      if (props.errors.keys.toList.contains("passwordMatch"))
        div("Password and confirmation should match.")
      else
        div()
    )

}
