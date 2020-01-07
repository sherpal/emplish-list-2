package models.errors

trait BackendException {

  def backendError: BackendError

}
