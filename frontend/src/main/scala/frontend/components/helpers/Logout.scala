package frontend.components.helpers

import frontend.utils.http.DefaultHttp._
import org.scalajs.dom
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.concurrent.ExecutionContext.Implicits._

@react final class Logout extends StatelessComponent {

  type Props = Unit

  def logout(): Unit =
    boilerplate
      .post(path("logout"))
      .send()
      .onComplete { _ =>
        dom.window.location.href = "/login"
      }

  def render(): ReactElement = button(onClick := logout _)("Log out")

}
