package streams.sources

import akka.NotUsed
import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.ownership.Owner

import scala.collection.mutable

object ReadFromEventStream {

  private class EventStreamSource[Out](eventStream: EventStream[Out]) extends GraphStage[SourceShape[Out]] {

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

      eventStream.foreach(inHandlerLike)(owner)

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

  private def apply[Out](stream: EventStream[Out]): Source[Out, NotUsed] =
    Source.fromGraph(new EventStreamSource[Out](stream))

  implicit final class SourceEnhanced(s: Source.type) {
    def readFromEventStream[Out](stream: EventStream[Out]): Source[Out, NotUsed] = apply(stream)
  }

}
