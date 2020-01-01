package frontend.forms

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, Materializer, QueueOfferResult}
import frontend.components.forms.SimpleForm
import frontend.forms.{FormData => SimpleFormData}
import models.errors.BackendError
import models.validators.FieldsValidator
import models.validators.StringValidators._
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.concurrent.Future

@react final class FormComponent extends Component {

  case class Props(actorSystem: ActorSystem)

  /** The state contains the form data.
    * This is only used for render function. Indeed, the actual state should only be contained in the
    * `scan` `formSource`.
    *
    */
  case class State(formData: FormData, errors: Map[String, List[BackendError]])

  implicit def system: ActorSystem = props.actorSystem
  implicit lazy val materializer: Materializer = ActorMaterializer()

  def initialState: State = State(formData = SimpleFormData("", ""), errors = Map())

  lazy val formSource: SourceQueueWithComplete[SimpleFormData => SimpleFormData] =
    SimpleForm[State, SimpleFormData](
      FieldsValidator(
        Map(
          "name" -> atLeastLength(6).contraMap[SimpleFormData](_.name),
          "email" -> emailValidator.contraMap[SimpleFormData](_.email)
        )
      ),
      Sink.foreach(form => setState(_.copy(formData = form))),
      Sink.foreach(errors => setState(_.copy(errors = errors)))
    ).run()

  /**
    * Handlers given to the [[InputForm]]s children so that they can modify the form data.
    */
  val changeName: String => Future[QueueOfferResult] = newName => formSource.offer(_.copy(name = newName))
  val changeEmail: String => Future[QueueOfferResult] = newEmail => formSource.offer(_.copy(email = newEmail))

  def render(): ReactElement = div(
    state.formData.display,
    InputForm("name", changeName, state.errors),
    InputForm("email", changeEmail, state.errors)
  )

}
