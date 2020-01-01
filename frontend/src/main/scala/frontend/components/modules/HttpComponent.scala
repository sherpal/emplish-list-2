package frontend.components.modules

import frontend.utils.http.Http
import sttp.client.{NothingT, SttpBackend}

import scala.concurrent.Future

trait HttpComponent {

  def http: Http

  implicit final val backend: SttpBackend[Future, Nothing, NothingT] = http.backend

}
