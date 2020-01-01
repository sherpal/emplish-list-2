package frontend.components.helpers.forms

import akka.stream.QueueOfferResult
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.concurrent.Future

object InputTextArea {

  def apply(
      title: String,
      v: String,
      updater: String => Future[QueueOfferResult]
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
