package frontend.components.login

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, SourceQueueWithComplete}
import akka.stream.{Materializer, QueueOfferResult}
import frontend.components.forms.SimpleForm
import frontend.components.helpers.forms.InputString
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.errors.BackendError
import models.users.LoginUser
import models.validators.FieldsValidator
import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import sttp.client.Response

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@react final class LoginForm extends Component {

  case class Props(actorSystem: ActorSystem, materializer: Materializer)

  implicit def as: ActorSystem = props.actorSystem
  implicit def mat: Materializer = props.materializer
  implicit def ec: ExecutionContext = as.dispatcher

  case class State(loginInfo: LoginUser, errors: Map[String, List[BackendError]], wrongCredentials: Boolean = false)

  def initialState: State = State(LoginUser("", ""), Map())

  lazy val simpleForm: SimpleForm[State, LoginUser] = SimpleForm[State, LoginUser](
    FieldsValidator.allowAllValidator,
    Sink.foreach(user => setState(_.copy(loginInfo = user))),
    Sink.foreach(errors => setState(_.copy(errors = errors)))
  )

  lazy val queue: SourceQueueWithComplete[simpleForm.FormDataChanger] = simpleForm.run()

  val changeName: String => Future[QueueOfferResult] = newName => queue.offer(_.copy(name = newName))
  val changePW: String => Future[QueueOfferResult] = newPassword => queue.offer(_.copy(password = newPassword))

  def submit(): Unit = if (state.errors.isEmpty) {
    boilerplate
      .post(path("login"))
      .body(state.loginInfo)
      .send()
      .onComplete {
        case Success(Response(Right(_), _, _, _, _)) =>
          dom.window.location.href = "/home"
        case Success(Response(Left(_), _, _, _, _)) =>
          setState(_.copy(wrongCredentials = true))
        case Failure(exception) =>
          throw exception
        case _ =>
          throw new Exception("Failure during de-serialization")
      }
  }

  def render(): ReactElement =
    form(
      onSubmit := (event => {
        event.preventDefault()
        submit()
      })
    )(
      InputString(state.loginInfo.name, "Name: ", changeName, simpleForm.sendErrors(List("name"), state.errors)),
      InputPassword("Password: ", changePW, state.errors),
      if (state.wrongCredentials) div("Incorrect username and/or password") else div(),
      input(`type` := "submit", value := "Log in")
    )

}
