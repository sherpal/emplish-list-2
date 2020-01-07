package frontend.components.login

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import frontend.components.forms.RegisterForm
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{a, div, h1, href}

@react final class Register extends StatelessComponent {

  type Props = Unit

  implicit lazy val actorSystem: ActorSystem = ActorSystem("Register")
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()

  override def componentWillUnmount(): Unit = {
    actorSystem.terminate()
  }

  def render(): ReactElement = div(
    h1("Register"),
    RegisterForm(actorSystem, materializer),
    a(href := "/login")("Login")
  )

}
