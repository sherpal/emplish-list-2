package frontend.laminar.components.login

import akka.actor.ActorSystem
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.components.forms.SimpleForm
import frontend.laminar.components.helpers.forms.InputString
import frontend.laminar.router.Router
import frontend.laminar.utils.ActorSystemContainer
import frontend.utils.http.DefaultHttp.{boilerplate, path, _}
import io.circe.generic.auto._
import models.errors.BackendError
import models.users.LoginUser
import models.validators.FieldsValidator
import org.scalajs.dom.html.Form
import sttp.client.Response
import syntax.WithUnit

import scala.util.{Failure, Success}

private[login] final class LoginForm(val validator: FieldsValidator[LoginUser, BackendError])(
    implicit val actorSystem: ActorSystem,
    val formDataWithUnit: WithUnit[LoginUser]
) extends Component[Form]
    with SimpleForm[LoginUser] {

  val wrongCredentials: Var[Boolean] = Var[Boolean](false)

  val element: ReactiveHtmlElement[Form] = {

    val $changeName = createFormDataChanger((name: String) => _.copy(name = name))
    val $changePW = createFormDataChanger((password: String) => _.copy(password = password))

    run()

    def submit(): Unit = {
      println("Submit!")

      val errorsNow = validator(formData.now)

      if (errorsNow.isEmpty) {
        boilerplate
          .post(path("login"))
          .body(formData.now)
          .send()
          .onComplete {
            case Success(Response(Right(_), _, _, _, _)) =>
              Router.router.moveTo("/home")
            case Success(Response(Left(_), _, _, _, _)) =>
              wrongCredentials.update(_ => true)
            case Failure(exception) =>
              throw exception
            case _ =>
              throw new Exception("Failure during de-serialization")
          }

      }
    }

    form(
      onSubmit.preventDefault --> (_ => submit()),
      InputString("Name ", formData.signal.map(_.name), $changeName),
      InputPassword("Password ", $changePW, $errors),
      child <-- wrongCredentials.signal.map(if (_) div("Incorrect username and/or password") else div()),
      input(tpe := "submit", value := "Log in")
    )
  }

}

object LoginForm {

  def apply(
      validator: FieldsValidator[LoginUser, BackendError] = LoginUser.validator
  )(implicit actorSystemContainer: ActorSystemContainer, formDataWithUnit: WithUnit[LoginUser]): LoginForm = {
    import actorSystemContainer.actorSystem
    new LoginForm(validator)
  }

}
