package frontend.laminar.components.login

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.utils.ActorSystemContainer
import org.scalajs.dom.html

object Register {

  def apply()(implicit actorSystemContainer: ActorSystemContainer): ReactiveHtmlElement[html.Div] = {

    import actorSystemContainer._

    div(
      h1("Register"),
      RegisterForm(),
      a(href := "/login", "Login")
    )
  }

}
