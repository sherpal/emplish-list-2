package frontend.laminar.components.login

import akka.actor.ActorSystem
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.components.forms.SimpleForm
import frontend.laminar.components.helpers.forms.InputString
import frontend.laminar.router.Router
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.errors.BackendError
import models.users.NewUser
import models.validators.FieldsValidator
import org.scalajs.dom.html.{Form, Progress}
import sttp.client.Response
import syntax.WithUnit

import scala.util.{Failure, Success}

final class RegisterForm private (
    val validator: FieldsValidator[NewUser, BackendError]
)(implicit val actorSystem: ActorSystem, val formDataWithUnit: WithUnit[NewUser])
    extends Component[Form]
    with SimpleForm[NewUser] {

  def submit(): Unit = {
    println("Submit!")

    val errorsNow = validator(formData.now)

    if (errorsNow.isEmpty) {
      boilerplate
        .post(path("register"))
        .body(formData.now)
        .response(asErrorOnly)
        .send()
        .onComplete {
          case Success(m: Response[_]) if m.isSuccess =>
            Router.router.moveTo("/after-registration")
          case Success(Response(Left(Right(backendErrors)), _, _, _, _)) =>
            errorsWriter.onNext(backendErrors)
          case Failure(exception) =>
            throw exception
          case _ =>
            throw new Exception("Failure during de-serialization")
        }

    } else {
      println("Do nothing")
    }
  }

  val $passwordStrength: Observable[Double] = formData.signal.map(_.passwordStrength)

  val passwordStrengthBar: ReactiveHtmlElement[Progress] = progress(
    value <-- $passwordStrength.map(_ * 100).map(_.toInt).map(_.toString),
    max := 100.toString,
    backgroundColor <-- $passwordStrength.map { // todo: style this properly
      case x if x <= 0.2 => "#ff0000"
      case x if x <= 0.6 => "#ff9900"
      case _             => "#00ff00"
    }
  )

  val element: ReactiveHtmlElement[Form] = {

    val $changeName = createFormDataChanger((newName: String) => _.copy(name = newName))
    val $changePW = createFormDataChanger((newPW: String) => _.copy(password = newPW))
    val $changeConfirmPW = createFormDataChanger((newPW: String) => _.copy(confirmPassword = newPW))
    val $changeEmail = createFormDataChanger((email: String) => _.copy(email = email))

    run() // todo[think] should this be done in the ComponentDidMount? probably...

    form(
      onSubmit.preventDefault --> (_ => submit()),
      fieldSet(
        InputString("Name ", formData.signal.map(_.name), $changeName)
      ),
      fieldSet(
        InputPassword("Password ", $changePW, $errors),
        passwordStrengthBar,
        InputPassword("Confirm password ", $changeConfirmPW, $errors)
      ),
      fieldSet(
        InputString("Email ", formData.signal.map(_.email), $changeEmail)
      ),
      input(tpe := "submit", value := "Sign up")
    )
  }
}

object RegisterForm {

  def apply(
      validator: FieldsValidator[NewUser, BackendError] = NewUser.fieldsValidator
  )(implicit actorSystem: ActorSystem, formDataWithUnit: WithUnit[NewUser]) = new RegisterForm(validator)

}
