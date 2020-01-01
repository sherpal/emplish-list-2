package frontend.forms

import akka.stream.QueueOfferResult
import frontend.components.forms.FrontendErrorSimpleDisplay
import models.errors.BackendError
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.concurrent.Future

@react final class InputForm extends StatelessComponent {

  case class Props(
      errorKey: String,
      changeFormData: String => Future[QueueOfferResult],
      errors: Map[String, List[BackendError]]
  )

  override def render(): ReactElement = div(
    props.errorKey,
    input(onChange := (event => props.changeFormData(event.target.value))),
    props.errors.toList.filter(_._1 == props.errorKey).flatMap(_._2).map(FrontendErrorSimpleDisplay(_))
  )

}
