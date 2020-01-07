package models.database

import java.util.UUID

import models.DBPendingRegistration
import models.errors.{BackendError, BackendException}
import models.users.PendingRegistration
import monix.eval.Task
import utils.database.DBProfile.api._
import utils.database.tables.PendingRegistrationTable
import utils.database.tables.UsersTable.{Password, UserName}

trait Registration extends MonixDB { self: Users =>

  private def query = PendingRegistrationTable.query

  def pendingRegistrations(limit: Int): Task[List[PendingRegistration]] =
    runAsTask(query.take(limit).result).map(_.toList).map(_.map(_.pendingRegistration))

  /**
    * Returns whether this `userName` is already in the "PendingRegistration" table.
    */
  def registrationIsPending(userName: UserName): Task[Boolean] =
    runAsTask(query.filter(_.name === userName).result.headOption).map(_.isDefined)

  def isUserNameUsed(userName: UserName): Task[Boolean] = exists(
    List(registrationIsPending(userName), userExists(userName)),
    Task.pure[Boolean]
  )

  /**
    * Adds the user to the pool of pending registration. An email should be sent to `emplish.list@gmail.com` to
    * notify this new registration.
    */
  def registerUser(userName: UserName, hashedPassword: Password, email: String): Task[String] =
    for {
      userNameAlreadyUsed <- isUserNameUsed(userName)
      randomKey = if (!userNameAlreadyUsed) UUID.randomUUID().toString.filterNot(_ == '-') else ""
      _ <- if (!userNameAlreadyUsed)
        runAsTask(query += DBPendingRegistration(userName, email, hashedPassword, randomKey))
      else Task.pure(())
    } yield randomKey

  def acceptUser(userName: UserName, randomKey: String): Task[Boolean] =
    for {
      maybeDbPendingRegistration <- runAsTask(
        query.filter(_.name === userName).filter(_.randomKey === randomKey).result.headOption
      )
      pendingRegistration <- Task {
        maybeDbPendingRegistration match {
          case Some(dBPendingRegistration) => dBPendingRegistration.pendingRegistration
          case None                        => throw new Registration.NoPendingRegistrationFor(userName)
        }
      }
      userAdded <- addUserPasswordHashed(pendingRegistration.name, pendingRegistration.hashedPassword)
      registrationDeleted <- deleteRegistration(userName, randomKey)
    } yield userAdded > 0 && registrationDeleted

  def deleteRegistration(userName: UserName, randomKey: String): Task[Boolean] =
    runAsTask(query.filter(_.randomKey === randomKey).filter(_.name === userName).delete).map(_ > 0)

  def rejectUser(userName: UserName, randomKey: String): Task[Boolean] = deleteRegistration(userName, randomKey)

  def registrationEmail(userName: UserName, randomKey: String): Task[Option[String]] =
    runAsTask(query.filter(_.name === userName).filter(_.randomKey === randomKey).map(_.email).result.headOption)

}

object Registration {

  final class NoPendingRegistrationFor(userName: UserName)
      extends Exception(s"$userName is not in the registering process")
      with BackendException {
    def backendError: BackendError = BackendError("noPendingRegistration", userName)
  }

}
