package models.errors

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}

case class BackendError(errorKey: String, message: String) extends Throwable(message)

object BackendError {
  def InvalidSessionError(message: String): BackendError = BackendError("backend.error.invalidSession", message)
  def UserNameAlreadyExists(userName: String): BackendError = BackendError("backend.error.nameAlreadyExists", userName)
  def InvalidNewUser(reason: String): BackendError = BackendError("backend.error.invalidNewUser", reason)

  implicit def errorsDecoder: Decoder[Map[String, List[BackendError]]] = Decoder.decodeMap[String, List[BackendError]]
  implicit def errorsEncoder: Encoder[Map[String, List[BackendError]]] = Encoder.encodeMap[String, List[BackendError]]
}
