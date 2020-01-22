package frontend.laminar.components.helpers

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html

object SimpleButton {

  def apply(label: String, click: () => Unit): ReactiveHtmlElement[html.Button] =
    button(onClick --> (_ => click()), label)

}
