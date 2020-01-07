package frontend.components.forms

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, SourceQueueWithComplete}
import frontend.components.helpers.forms.InputString
import frontend.components.helpers.miscellaneous.StatusBar
import frontend.components.helpers.miscellaneous.StatusBar.StatusBarOptions
import frontend.components.login.InputPassword
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.errors.BackendError
import models.users.NewUser
import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import sttp.client.Response
import syntax.WithUnit

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@react final class RegisterForm extends Component {

  case class Props(actorSystem: ActorSystem, materializer: Materializer)

  case class State(newUser: NewUser, errors: Map[String, List[BackendError]], submitted: Boolean = false)

  override def initialState: State = State(implicitly[WithUnit[NewUser]].unit, Map())

  implicit def as: ActorSystem = props.actorSystem
  implicit def mat: Materializer = props.materializer
  implicit def ec: ExecutionContext = as.dispatcher

  lazy val simpleForm: SimpleForm[State, NewUser] = SimpleForm[State, NewUser](
    NewUser.fieldsValidator,
    Sink.foreach(user => setState(_.copy(newUser = user))),
    Sink.foreach(errors => setState(_.copy(errors = errors)))
  )(WithUnit(initialState.newUser))

  lazy val queue: SourceQueueWithComplete[simpleForm.FormDataChanger] = simpleForm.run()

  val changeName: String => Unit = newName => queue.offer(_.copy(name = newName))
  val changePW: String => Unit = newPassword => queue.offer(_.copy(password = newPassword))
  val changeConfirmPW: String => Unit = newConfirmPW => queue.offer(_.copy(confirmPassword = newConfirmPW))
  val changeEmail: String => Unit = newEmail => queue.offer(_.copy(email = newEmail))

  def submit(): Unit = {
    setState(_.copy(submitted = true))
    val errors = simpleForm.validator(state.newUser)

    if (errors.isEmpty) {
      boilerplate
        .post(path("register"))
        .body(state.newUser)
        .response(asErrorOnly)
        .send()
        .onComplete {
          case Success(m: Response[_]) if m.isSuccess =>
            dom.window.location.href = "/login"
          case Success(Response(Left(Right(backendErrors)), _, _, _, _)) =>
            setState(_.copy(errors = backendErrors))
          case Failure(exception) =>
            throw exception
          case _ =>
            throw new Exception("Failure during de-serialization")
        }

    } else {
      println("Do nothing")
    }
  }

  def errorsForChildren: Map[String, List[BackendError]] =
    if (state.submitted) state.errors else Map[String, List[BackendError]]()

  def passwordStrengthBar: ReactElement = StatusBar(
    state.newUser.passwordStrength * 100,
    StatusBarOptions(colour = (_: Double) match {
      case x if x <= 20 => "#ff0000"
      case x if x <= 60 => "#ff9900"
      case _            => "#00ff00"
    })
  )

  def render(): ReactElement =
    form(
      onSubmit := (event => {
        event.preventDefault()
        submit()
      })
    )(
      fieldset(
        InputString(state.newUser.name, "Name ", changeName, simpleForm.sendErrors(List("name"), errorsForChildren))
      ),
      fieldset(
        InputPassword("Password ", changePW, errorsForChildren),
        passwordStrengthBar,
        InputPassword("Confirm Password ", changeConfirmPW, errorsForChildren)
      ),
      fieldset(
        InputString(state.newUser.email, "Email ", changeEmail, Nil)
      ),
      input(`type` := "submit", value := "Sign up")
    )

}
