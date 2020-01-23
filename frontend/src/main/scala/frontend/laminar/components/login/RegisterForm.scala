package frontend.laminar.components.login

import akka.actor.ActorSystem
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.forms.SimpleForm
import frontend.laminar.components.helpers.forms.InputString
import frontend.laminar.router.Router
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.errors.BackendError
import models.users.NewUser
import org.scalajs.dom.html.Form
import sttp.client.Response
import syntax.WithUnit

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object RegisterForm {

  def apply()(implicit actorSystem: ActorSystem): ReactiveHtmlElement[Form] = {

    implicit val owner: Owner = new Owner {}
    implicit def ec: ExecutionContext = actorSystem.dispatcher

    val newUser = Var[NewUser](implicitly[WithUnit[NewUser]].unit)
    val errors = new EventBus[Map[String, List[BackendError]]]()

    val newUserChangerBus = new EventBus[NewUser => NewUser]()
    val $changeName = newUserChangerBus.writer.contramapWriter((newName: String) => _.copy(name = newName))
    val $changePW = newUserChangerBus.writer.contramapWriter((newPW: String) => _.copy(password = newPW))
    val $changeConfirmPW = newUserChangerBus.writer.contramapWriter((newPW: String) => _.copy(confirmPassword = newPW))
    val $changeEmail = newUserChangerBus.writer.contramapWriter((email: String) => _.copy(email = email))
    val newUserBus = new EventBus[NewUser]()
    newUserBus.events.foreach(user => newUser.update(_ => user))

    newUserChangerBus.events.map("Changer: " + _).foreach(println)

    val simpleForm = new SimpleForm[NewUser](
      newUserChangerBus.events,
      newUserBus.writer,
      Some(errors.writer),
      NewUser.fieldsValidator
    )
    simpleForm.run()

    def submit(): Unit = {
      println("Submit!")

      val errorsNow = simpleForm.validator(newUser.now)

      if (errorsNow.isEmpty) {
        boilerplate
          .post(path("register"))
          .body(newUser.now)
          .response(asErrorOnly)
          .send()
          .onComplete {
            case Success(m: Response[_]) if m.isSuccess =>
              Router.router.moveTo("/login") // todo: view to say that every thing was ok and a mail will be sent
            case Success(Response(Left(Right(backendErrors)), _, _, _, _)) =>
              errors.writer.onNext(backendErrors)
            case Failure(exception) =>
              throw exception
            case _ =>
              throw new Exception("Failure during de-serialization")
          }

      } else {
        println("Do nothing")
      }
    }

    val passwordStrengthBar = progress(
      value <-- newUser.signal.map(_.passwordStrength * 100).map(_.toInt).map(_.toString),
      max := 100.toString,
      backgroundColor <-- newUser.signal.map(_.passwordStrength).map { // todo: style this properly
        case x if x <= 0.2 => "#ff0000"
        case x if x <= 0.6 => "#ff9900"
        case _             => "#00ff00"
      }
    )

    form(
      onSubmit.preventDefault --> (_ => submit()),
      fieldSet(
        InputString("Name ", newUser.signal.map(_.name), $changeName)
      ),
      fieldSet(
        InputPassword("Password ", $changePW, errors.events),
        passwordStrengthBar,
        InputPassword("Confirm password ", $changeConfirmPW, errors.events)
      ),
      fieldSet(
        InputString("Email ", newUser.signal.map(_.email), $changeEmail)
      ),
      input(tpe := "submit", value := "Sign up")
    )
  }

}
