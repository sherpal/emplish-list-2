package models.errors

final case class FrontendError(errorKey: String, message: String) extends Throwable(message)
