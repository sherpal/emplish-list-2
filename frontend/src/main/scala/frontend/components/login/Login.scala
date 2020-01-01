package frontend.components.login

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class Login extends StatelessComponent {

  type Props = Unit

  implicit lazy val actorSystem: ActorSystem = ActorSystem("Register")
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()

  override def componentWillUnmount(): Unit = {
    actorSystem.terminate()
  }

  def render(): ReactElement = div(
    h1("Login"),
    LoginForm(actorSystem, materializer),
    a(href := "/sign-up")("Sign up")
  )

}
