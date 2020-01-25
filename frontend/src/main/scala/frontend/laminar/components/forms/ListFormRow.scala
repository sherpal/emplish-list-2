package frontend.laminar.components.forms

import com.raquo.airstream.eventbus.{EventBus, WriteBus}
import com.raquo.airstream.eventstream.EventStream
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import org.scalajs.dom
import org.scalajs.dom.html.Span

final class ListFormRow[T](
    current: T,
    writer: WriteBus[T],
    elementCreator: (T, WriteBus[T]) => ReactiveHtmlElement[dom.html.Span]
) extends Component[dom.html.Span] {

  override val element: ReactiveHtmlElement[Span] = elementCreator(current, writer)

}
