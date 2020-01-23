package streams.sinks

import akka.Done
import akka.stream.scaladsl.Sink
import com.raquo.laminar.api.L._

import scala.concurrent.Future

object WriteToBus {

  private[sinks] def apply[In](writeBus: WriteBus[In]): Sink[In, Future[Done]] =
    Sink.foreach((in: In) => {
      println("writring to bus: " + in)
      writeBus.onNext(in)
    })

  implicit final class SinkEnhanced(sink: Sink.type) {

    def writeToBus[In](writeBus: WriteBus[In]): Sink[In, Future[Done]] = apply(writeBus)

  }

}
