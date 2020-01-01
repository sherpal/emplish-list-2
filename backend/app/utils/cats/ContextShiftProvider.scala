package utils.cats

import cats.effect.{ContextShift, IO}

import scala.concurrent.ExecutionContext

trait ContextShiftProvider {

  def ec: ExecutionContext

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ec)

}
