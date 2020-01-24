package frontend.laminar.components.login

import akka.actor.ActorSystem
import frontend.laminar.utils.ActorSystemContainer
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.forms.SimpleForm
import frontend.laminar.components.helpers.forms.InputString
import frontend.laminar.router.Router
import frontend.utils.http.DefaultHttp.{boilerplate, path}
import models.errors.BackendError
import models.users.LoginUser
import org.scalajs.dom
import org.scalajs.dom.html.Form
import sttp.client.Response
import syntax.WithUnit
import io.circe.generic.auto._
import frontend.utils.http.DefaultHttp._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

private[login] object LoginForm {

  def apply()(implicit actorSystemContainer: ActorSystemContainer): ReactiveHtmlElement[Form] = {

    implicit val owner: Owner = new Owner {}

    import actorSystemContainer._

    val loginInfo = Var[LoginUser](implicitly[WithUnit[LoginUser]].unit)
    val errors = new EventBus[Map[String, List[BackendError]]]()
    val wrongCredentials = Var[Boolean](false)
    val loginInfoBus = new EventBus[LoginUser]()
    loginInfoBus.events.foreach(login => loginInfo.update(_ => login))

    val loginUserChangerBus = new EventBus[LoginUser => LoginUser]()
    val $changeName = loginUserChangerBus.writer.contramapWriter((name: String) => _.copy(name = name))
    val $changePW = loginUserChangerBus.writer.contramapWriter((password: String) => _.copy(password = password))

    val simpleForm: SimpleForm[LoginUser] = new SimpleForm[LoginUser](
      loginUserChangerBus.events,
      loginInfoBus.writer,
      Some(errors.writer),
      LoginUser.validator
    )
    simpleForm.run()

    def submit(): Unit = {
      println("Submit!")

      val errorsNow = simpleForm.validator(loginInfo.now)

      if (errorsNow.isEmpty) {
        boilerplate
          .post(path("login"))
          .body(loginInfo.now)
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
      InputString("Name ", loginInfo.signal.map(_.name), $changeName),
      InputPassword("Password ", $changePW, errors.events),
      child <-- wrongCredentials.signal.map(if (_) div("Incorrect username and/or password") else div()),
      input(tpe := "submit", value := "Log in")
    )
  }

}
