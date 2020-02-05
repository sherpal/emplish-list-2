package streams.sources

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.{ImplicitSender, TestKit}
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.ownership.Owner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import streams.sinks.WriteToObserver._
import streams.sources.ReadFromObservable._

import scala.concurrent.duration._

final class SourceFromLaminarEventStream()
    extends TestKit(ActorSystem("SourceFromLaminarEventStreamSpec"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A Sink to bus" must {

    "correctly pass the elements to the bus" in {

      val bus = new EventBus[Int]()
      bus.events.foreach(println)(new Owner {})

      Source(List(1, 2, 3)).runWith(Sink.writeToObserver(bus.writer))

    }

  }

  "A Source from EventStream" must {

    "correctly forward arriving message" in {

      val source1 = Source(List(1, 2, 3)).delay(1.second)
      val bus = new EventBus[Int]()

      source1.runWith(Sink.writeToObserver(bus.writer))

      val source2 = Source.readFromObservable(bus.events)

      source2.take(3).runWith(TestSink.probe[Int]).expectNext(1, 2, 3).expectComplete()
      //source2.take(3).runWith(Sink.foreach(x => println("hey: " + x)))
      println("finished")

    }

  }

}
