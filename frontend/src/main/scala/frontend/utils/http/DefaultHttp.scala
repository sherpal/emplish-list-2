package frontend.utils.http

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import models.errors.BackendError
import org.scalajs.dom.document
import sttp.client._
import sttp.model.{MediaType, MultiQueryParams, Uri}
import org.scalajs.dom

import scala.concurrent.Future

object DefaultHttp extends Http {

  implicit val backend: SttpBackend[Future, Nothing, NothingT] = FetchBackend()

  final val csrfTokenName = "Csrf-Token"

  def maybeCsrfToken: Option[String] =
    dom.document.cookie
      .split(";")
      .map(_.trim)
      .find(_.startsWith(s"$csrfTokenName="))
      .map(_.drop(csrfTokenName.length + 1))

  def boilerplate: RequestT[Empty, Either[String, String], Nothing] =
    basicRequest.header("Csrf-Token", maybeCsrfToken.getOrElse("none"))

  def host: Uri = Uri.parse(document.location.origin.toString).right.get //uri"http://localhost:8080"

  def path(s: String, ss: String*): Uri = host.path("api", s, ss: _*)

  def pathWithMultipleParams(params: Map[String, List[String]], s: String, ss: String*): Uri =
    path(s, ss: _*).params(MultiQueryParams.fromMultiMap(params))

  def responseAs[A](
      implicit aDecoder: Decoder[A]
  ): ResponseAs[Either[Either[Error, Map[String, List[BackendError]]], Either[Error, A]], Nothing] = asEither(
    asStringAlways.map(
      decode[Map[String, List[BackendError]]](_)
    ),
    asStringAlways.map(
      decode[A]
    )
  )

  def asErrorOnly: ResponseAs[Either[Either[Error, Map[String, List[BackendError]]], Unit], Nothing] =
    asEither(asStringAlways.map(decode[Map[String, List[BackendError]]](_)), ignore)

  implicit def bodySerializer[A](implicit aEncoder: Encoder[A]): A => BasicRequestBody =
    (a: A) =>
      StringBody(
        a.asJson.noSpaces,
        "utf-8",
        Some(MediaType.ApplicationJson)
      )

}
