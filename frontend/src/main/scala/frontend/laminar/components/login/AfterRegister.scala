package frontend.laminar.components.login

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.router.Link
import org.scalajs.dom
import org.scalajs.dom.html.Div
import urldsl.language.PathSegment.dummyErrorImpl._

final class AfterRegister private () extends Component[dom.html.Div] {
  val element: ReactiveHtmlElement[Div] = div(
    h1("Thanks for registering to Emplish list!"),
    p(
      "You request has been sent to our teams in order to handle it."
    ),
    p(
      "Once our team has processed your request, you will receive an email inviting you to login."
    ),
    p(
      Link(to = root / "login")(text = "Login")
    )
  )
}

object AfterRegister {
  def apply(): AfterRegister = new AfterRegister()
}
