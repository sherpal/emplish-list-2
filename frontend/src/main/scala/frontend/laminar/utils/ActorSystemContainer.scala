package frontend.laminar.utils

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.ExecutionContext

final class ActorSystemContainer {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit def ec: ExecutionContext = actorSystem.dispatcher

}

object ActorSystemContainer {

  implicit lazy final val default: ActorSystemContainer = new ActorSystemContainer

}
