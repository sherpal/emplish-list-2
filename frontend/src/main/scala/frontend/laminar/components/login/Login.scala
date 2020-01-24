package frontend.laminar.components.login

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html

object Login {

  def apply(): ReactiveHtmlElement[html.Div] = div(
    h1("Login"),
    LoginForm(),
    a(href := "/sign-up", "Sign up")
  )

}
