package frontend.components.helpers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.ExecutionContext

trait ComponentWithActorSystem {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()
  implicit def ec: ExecutionContext = actorSystem.dispatcher

}
