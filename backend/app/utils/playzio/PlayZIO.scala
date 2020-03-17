package utils.playzio

import play.api.mvc.{Action, ActionBuilder, AnyContent, BodyParser, Request, Result}
import zio.{Has, Runtime, Tagged, URIO, ZIO, ZLayer}

import scala.language.higherKinds

object PlayZIO {

  type ZIORequestA[R[_], A] = Has[R[A]]
  type ZIORequest[A] = Has[Request[A]]
  type ZIOAnyContent[R[_]] = Has[R[AnyContent]]
  type ZIOSimpleRequest = Has[Request[AnyContent]]

  def request[R[_], A](implicit tagged: Tagged[R[A]]): ZIO[Has[R[A]], Nothing, R[A]] = {
    ZIO.access(_.get)
  }

  final implicit class ZIOAction[R[_], B](actionBuilder: ActionBuilder[R, B]) {

    def zio[A](
        bodyParser: BodyParser[A]
    )(block: ZIO[Has[R[A]], Nothing, Result])(implicit tagged: Tagged[R[A]]): Action[A] =
      actionBuilder.async(bodyParser) { req =>
        Runtime.default.unsafeRunToFuture(
          block.provideLayer(ZLayer.succeed(req))
        )
      }

    def zio(block: ZIO[Has[R[B]], Nothing, Result])(implicit tagged: Tagged[R[B]]): Action[B] =
      actionBuilder.async { req =>
        Runtime.default.unsafeRunToFuture(block.provideLayer(ZLayer.succeed(req)))
      }

  }

}
