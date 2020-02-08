package frontend.laminar.components.tests

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import org.scalajs.dom
import org.scalajs.dom.html.Div
import streams.laminar.streams.operators.CombineLatest

final class TestComponent private () extends Component[dom.html.Div] {
  val element: ReactiveHtmlElement[Div] = {

    div(
      h1("This is the test!"), {
        val left = new EventBus[String]()
        val right = new EventBus[String]()

        section(
          h2("Combine Latest"),
          input(inContext(elem => onInput.mapTo(elem.ref.value) --> left.writer)),
          input(inContext(elem => onInput.mapTo(elem.ref.value) --> right.writer)),
          div(
            "Combine latest: ",
            child <-- CombineLatest(left.events, right.events).map(_.toString)
          ),
          div(
            "Combine with",
            child <-- left.events.combineWith(right.events).map(_.toString)
          )
        )
      }, {
        val left = Var("left")
        val right = Var("right")

        val combined = CombineLatest(left.signal, right.signal, left.now, right.now)

        section(
          h2("Combine latest for signals"),
          input(value <-- combined.map(_._1), inContext(elem => onInput.mapTo(elem.ref.value) --> left.writer)),
          input(value <-- combined.map(_._2), inContext(elem => onInput.mapTo(elem.ref.value) --> right.writer)),
          div(
            "CombineLatest: ",
            child <-- combined.map(_.toString)
          ),
          div(
            "CombineWidth: ",
            child <-- left.signal.combineWith(right.signal).map(_.toString)
          )
        )
      }
    )
  }
}

object TestComponent {
  def apply(): TestComponent = new TestComponent
}
