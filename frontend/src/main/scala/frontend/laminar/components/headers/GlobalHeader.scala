package frontend.laminar.components.headers

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.helpers.Logout
import models.users.User
import org.scalajs.dom.html

object GlobalHeader {

  def apply(user: User): ReactiveHtmlElement[html.Element] = header(span("Emplish List"), span(user.name), Logout())

}
