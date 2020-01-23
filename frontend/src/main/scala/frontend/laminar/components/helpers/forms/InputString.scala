package frontend.laminar.components.helpers.forms

import com.raquo.airstream.eventbus.WriteBus
import com.raquo.airstream.eventstream.EventStream
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html

object InputString {

  def apply(
      title: String,
      values: Signal[String],
      $formDataChanger: Observer[String]
  ): ReactiveHtmlElement[html.Div] = div(
    className := "input-string",
    title,
    input(
      value <-- values,
      inContext(thisNode => onInput.mapTo(thisNode.ref.value) --> $formDataChanger)
    )
  )

}
