package frontend.utils.http

import sttp.client._
import sttp.model.Uri

import scala.concurrent.Future

trait Http {

  implicit val backend: SttpBackend[Future, Nothing, NothingT]

  def boilerplate: RequestT[Empty, Either[String, String], Nothing]

  def host: Uri

  def path(s: String, ss: String*): Uri

}
