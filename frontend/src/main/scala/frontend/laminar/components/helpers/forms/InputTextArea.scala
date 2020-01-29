package frontend.laminar.components.helpers.forms

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html.Paragraph

object InputTextArea {

  def apply(title: String, values: Observable[String], valuesWriter: Observer[String]): ReactiveHtmlElement[Paragraph] =
    p(
      title,
      br(),
      textArea(value <-- values, inContext(thisNode => onInput.mapTo(thisNode.ref.value) --> valuesWriter))
    )

}
