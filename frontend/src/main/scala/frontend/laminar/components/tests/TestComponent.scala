package frontend.laminar.components.tests

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import org.scalajs.dom
import org.scalajs.dom.html.Div
import streams.laminar.CombineLatest

final class TestComponent private () extends Component[dom.html.Div] {
  val element: ReactiveHtmlElement[Div] = {

    val left = new EventBus[String]()
    val right = new EventBus[String]()

    div(
      h1("This is the test!"),
      section(
        h2("Combine Latest"),
        input(inContext(elem => onInput.mapTo(elem.ref.value) --> left.writer)),
        input(inContext(elem => onInput.mapTo(elem.ref.value) --> right.writer)),
        child <-- CombineLatest(left.events, right.events).map(_.toString).map(span(_))
      )
    )
  }
}

object TestComponent {
  def apply(): TestComponent = new TestComponent
}
