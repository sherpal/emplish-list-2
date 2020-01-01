package frontend.components.helpers.lab

import org.scalajs.dom.html
import org.scalajs.dom.html.Span
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.{React, ReactElement, ReactRef}
import slinky.web.html.{span, _}

import scala.concurrent.duration._
import scala.scalajs.js.timers._
import scala.util.Random
@react final class ChangingRandomSpan extends Component {

  type Props = Unit

  case class State(
      currentValue: Int = 0,
      intervalHandle: Option[SetIntervalHandle] = None,
      underlyingSpan: Option[html.Span] = None
  )
  def initialState: State = State()

  val myRef: ReactRef[Span] = React.createRef[html.Span]

  override def componentWillMount(): Unit = {
    val handle = setInterval(3.seconds) {
      setState(_.copy(currentValue = Random.nextInt()))
    }

    setState(_.copy(intervalHandle = Some(handle)))
  }

  override def componentDidMount(): Unit = {
    setState(_.copy(underlyingSpan = Some(myRef.current)))
  }

  override def componentWillUnmount(): Unit = {
    state.intervalHandle match {
      case Some(handle) => clearInterval(handle)
      case None         =>
    }
  }

  def render(): ReactElement = span(ref := myRef)("Current value: ", state.currentValue.toString)

}
