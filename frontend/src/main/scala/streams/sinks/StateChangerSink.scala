package streams.sinks

import akka.Done
import akka.stream.scaladsl.Sink
import slinky.core.Component

import scala.concurrent.Future

object StateChangerSink {

  private def apply[In](
      component: Component
  )(stateChanger: In => component.State => component.State): Sink[In, Future[Done]] =
    Sink.foreach[In](in => component.setState(stateChanger(in)))

  implicit final class SinkEnhanced(sink: Sink.type) {

    def stateChanger[In](
        component: Component
    )(stateChanger: In => component.State => component.State): Sink[In, Future[Done]] =
      apply(component)(stateChanger)

  }

}
