package frontend.laminar.components.helpers

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.router.Router
import frontend.utils.http.DefaultHttp._
import org.scalajs.dom.html

import scala.concurrent.ExecutionContext.Implicits._

object Logout {

  private def logout(): Unit =
    boilerplate
      .post(path("logout"))
      .send()
      .onComplete { _ =>
        Router.router.moveTo("/login")
      }

  def apply(): ReactiveHtmlElement[html.Button] = button(onClick --> (_ => logout()), "Log out")

}
