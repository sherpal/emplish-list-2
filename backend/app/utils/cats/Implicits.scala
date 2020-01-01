package utils.cats

import cats.effect.IO

import scala.concurrent.{ExecutionContext, Future, Promise}

object Implicits {

  implicit class IOEnhanced[T](io: IO[T]) {

    def runToFuture()(implicit ec: ExecutionContext): Future[T] = {
      val promise = Promise[T]()

      io.unsafeRunAsync {
        case Left(throwable) => promise.failure(throwable)
        case Right(value) => promise.success(value)
      }

      promise.future
    }

  }

}
