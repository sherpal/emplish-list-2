package frontend.laminar.components.helpers

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html

object InputSearch {

  def apply[T](
      values: EventBus[T],
      elements: List[T],
      filter: T => T => Boolean,
      decoder: String => T,
      printer: T => String = (_: T).toString,
      isValid: Option[T => Boolean] = None
  ): ReactiveHtmlElement[html.Span] = {
    val filteredElements = values.events.map(filter).map(elements.filter)

    val idString = java.util.UUID.randomUUID().toString

    span(
      input(
        className <-- values.events.map(t => isValid.map(_(t))).map(_.map(if (_) "valid" else "invalid").getOrElse("")),
        tpe := "text",
        value <-- values.events.map(printer),
        inContext(thisNode => onInput.mapTo(thisNode.ref.value).map(decoder) --> values.writer),
        listId := idString // link to the dataList below
      ),
      dataList(
        id := idString,
        children <-- filteredElements.map(_.take(5).map(printer).map(value := _).map(option(_)))
      )
    )

  }

}
