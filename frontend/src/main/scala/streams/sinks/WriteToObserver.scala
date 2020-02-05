package streams.sinks

import akka.Done
import akka.stream.scaladsl.Sink
import com.raquo.laminar.api.L._

import scala.concurrent.Future

object WriteToObserver {

  private[sinks] def apply[In](observer: Observer[In]): Sink[In, Future[Done]] =
    Sink.foreach((in: In) => observer.onNext(in))

  implicit final class SinkEnhanced(sink: Sink.type) {

    def writeToObserver[In](observer: Observer[In]): Sink[In, Future[Done]] = apply(observer)

  }

}
