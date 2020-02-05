package streams.sources

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}
import com.raquo.airstream.core.Observable
import com.raquo.airstream.ownership.Owner

import scala.collection.mutable

object ReadFromObservable {

  private class EventStreamSource[Out](observable: Observable[Out]) extends GraphStage[SourceShape[Out]] {

    val outlet: Outlet[Out] = Outlet("EventStreamSource")

    def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

      private val accumulated = mutable.Queue[Out]()
      var downstreamWaiting = false

      private val owner = new Owner {}

      def inHandlerLike(element: Out): Unit = {
        accumulated.enqueue(element)
        if (downstreamWaiting) {
          val elem = accumulated.dequeue()
          push(outlet, elem)
          downstreamWaiting = false
        }
      }

      observable.foreach(inHandlerLike)(owner)

      setHandler(
        outlet,
        new OutHandler {
          def onPull(): Unit = {
            if (accumulated.isEmpty) {
              downstreamWaiting = true
            } else {
              val elem = accumulated.dequeue()
              push(outlet, elem)
            }
          }
        }
      )

    }

    def shape: SourceShape[Out] = SourceShape(outlet)
  }

  private def apply[Out](observable: Observable[Out]): Source[Out, NotUsed] =
    Source.fromGraph(new EventStreamSource[Out](observable))

  implicit final class SourceEnhanced(s: Source.type) {
    def readFromObservable[Out](observable: Observable[Out]): Source[Out, NotUsed] = apply(observable)
  }

}
