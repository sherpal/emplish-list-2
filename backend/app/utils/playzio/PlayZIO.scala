package utils.playzio

import play.api.mvc.{Action, ActionBuilder, AnyContent, BodyParser, Result}
import zio.{Has, Runtime, URIO, ZIO, ZLayer}

import scala.language.higherKinds

object PlayZIO {

  final implicit class ZIOAction[R[_], B](actionBuilder: ActionBuilder[R, B]) {

    def zio[A](bodyParser: BodyParser[A])(block: R[A] => ZIO[Any, Nothing, Result]): Action[A] =
      actionBuilder.async(bodyParser) { req =>
        Runtime.default.unsafeRunToFuture(
          block(req)
        )
      }

    def zio(block: R[B] => ZIO[Any, Nothing, Result]): Action[B] =
      actionBuilder.async { req =>
        Runtime.default.unsafeRunToFuture(block(req))
      }

    def zio(block: ZIO[Any, Nothing, Result]): Action[AnyContent] =
      actionBuilder { Runtime.default.unsafeRun(block) }

  }

}
