package streams.sources

import akka.NotUsed
import akka.stream.{Attributes, Outlet, OverflowStrategy, SourceShape}
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
        println("received an element: " + element)
        if (downstreamWaiting) {
          println("pushing")
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
            println("coucou")
            if (accumulated.isEmpty) {
              println("accumulated was empty")
              downstreamWaiting = true
            } else {
              println("accumulated was not empty")
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
