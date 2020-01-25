package frontend.laminar.components.helpers

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html

object InputSearch {

  def apply[T](
      values: Observable[T],
      elements: List[T],
      filter: T => T => Boolean,
      decoder: String => T,
      printer: T => String,
      valuesWriter: Observer[T],
      isValid: Option[T => Boolean] = None
  ): ReactiveHtmlElement[html.Span] = {
    val filteredElements = values.map(filter).map(elements.filter)

    val idString = java.util.UUID.randomUUID().toString

    val inputValidClass =
      values.map(t => isValid.map(_(t))).map(_.map(if (_) "valid" else "invalid").getOrElse(""))

    span(
      input(
        className <-- inputValidClass,
        tpe := "text",
        value <-- values.map(printer),
        inContext(thisNode => onInput.mapTo(thisNode.ref.value).map(decoder) --> valuesWriter),
        listId := idString // link to the dataList below
      ),
      dataList(
        id := idString,
        children <-- filteredElements.map(_.take(100).map(printer).map(value := _).map(option(_)))
      )
    )

  }

}
