package utils.mail

import javax.mail.internet.InternetAddress
import models.errors.{BackendError, BackendException}
import models.users.PendingRegistration
import monix.eval.Task
import views.html
import utils.config.ConfigRequester.|>

trait InviteEmails {

  def origin: String = (|> >> "origin").into[String]

  private def mailObject =
    for {
      maybeMail <- Task.pure(Mail.mail)
      mail = if (maybeMail.isEmpty) throw new InviteEmails.EmailPasswordNotSet else maybeMail.get
    } yield mail

  implicit private final class RecoverFromBackendException[T](task: Task[Either[BackendError, T]]) {

    def recoverFromBackendException: Task[Either[BackendError, T]] = task.onErrorRecover {
      case e: BackendException => Left[BackendError, T](e.backendError)
    }

  }

  def newRegistration(pendingRegistration: PendingRegistration): Task[Either[BackendError, Boolean]] = {
    (for {
      mail <- mailObject
      _ <- Task.fromTry(
        mail.sendTwirlEmail[PendingRegistration](
          Mail.mailAddress,
          "New Registration",
          (registration: PendingRegistration) =>
            html.registrationemail(
              registration.name,
              registration.email,
              registration.randomKey,
              origin
            ),
          pendingRegistration
        )
      )
    } yield Right[BackendError, Boolean](true)).recoverFromBackendException
  }

  def registrationAccepted(userName: String, email: String): Task[Either[BackendError, Boolean]] = {
    (for {
      mail <- mailObject
      _ <- Task.fromTry(
        mail.sendTwirlEmail(
          new InternetAddress(email),
          "Welcome to Emplish List!",
          (_: Unit) => html.welcomeemail(userName, origin)
        )
      )
    } yield Right[BackendError, Boolean](true)).recoverFromBackendException
  }

}

object InviteEmails {

  final class EmailPasswordNotSet extends Exception("Password for the email is not known") with BackendException {
    def backendError: BackendError = BackendError("emailPasswordUnknown", "")
  }

  final class MailFailedToPass(message: String) extends Exception(message) with BackendException {
    def backendError: BackendError = BackendError("mailFailed", message)
  }

}
