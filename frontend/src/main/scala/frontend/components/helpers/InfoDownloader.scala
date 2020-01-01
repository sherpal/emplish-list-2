package frontend.components.helpers

import frontend.utils.http.DefaultHttp._
import io.circe
import models.errors.BackendError
import sttp.client.{Identity, RequestT}

import scala.concurrent.ExecutionContext
import scala.util.Success

trait InfoDownloader[State] {

  implicit def ec: ExecutionContext

  def setState(changer: State => State): Unit

  def defaultPath: String

  private def request[T](p: String)(
      implicit aDecoder: io.circe.Decoder[Vector[T]]
  ): RequestT[Identity, Either[Either[circe.Error, Map[String, List[BackendError]]], Either[circe.Error, Vector[T]]], Nothing] =
    boilerplate
      .get(path(defaultPath, p))
      .response(responseAs[Vector[T]])

  private def requestWithParams[R](p: String, params: Map[String, List[String]])(
      implicit aDecoder: io.circe.Decoder[R]
  ) =
    boilerplate
      .get(pathWithMultipleParams(params, defaultPath, p))
      .response(responseAs[R])

  def downloadInfo[T](p: String, stateChanger: Vector[T] => State => State)(
      implicit aDecoder: io.circe.Decoder[Vector[T]]
  ): Unit =
    request(p)
      .send()
      .map(_.body)
      .onComplete {
        case Success(Right(Right(t))) =>
          println(p + ": " + t.mkString(", "))
          setState(stateChanger(t))
        case _ =>
          println("Something went wrong for " + p)
      }

  def downloadInfoWithParams[R](p: String, params: Map[String, List[String]], stateChanger: R => State => State)(
      implicit aDecoder: io.circe.Decoder[R]
  ): Unit =
    requestWithParams(p, params)
      .send()
      .map(_.body)
      .onComplete {
        case Success(Right(Right(r))) =>
          setState(stateChanger(r))
        case _ =>
          println("something went wrong for " + p)
      }

}

object InfoDownloader {

  def apply[State](
      defaultPath: String,
      stateSetter: (State => State) => Unit
  )(implicit executionContext: ExecutionContext): InfoDownloader[State] = {
    val _defaultPath = defaultPath
    new InfoDownloader[State] {
      val ec: ExecutionContext = executionContext

      def defaultPath: String = _defaultPath

      def setState(changer: State => State): Unit = stateSetter(changer)
    }
  }

}
