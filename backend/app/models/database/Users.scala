package models.database

import java.util.UUID

import cats.syntax.applicative._
import models.DBUser
import monix.eval.Task
import org.mindrot.jbcrypt.BCrypt
import utils.database.DBProfile.api._
import utils.database.tables.UsersTable.{Password, UserName, hashPassword, query}

trait Users extends MonixDB {

  private def randomUUID: Task[String] =
    UUID
      .randomUUID()
      .toString
      .pure[Task]
      .flatMap(
        uuid => Task.parZip2(runAsTask(query.filter(_.id === uuid).result.headOption), uuid.pure[Task])
      )
      .restartUntil(_._1.isEmpty)
      .map(_._2)

  def addUser(dBUser: DBUser): Task[Int] = runAsTask(query += dBUser)

  def addUser(userName: UserName, password: Password): Task[Vector[DBUser]] =
    randomUUID
      .flatMap(uuid => addUser(DBUser(uuid, userName, hashPassword(password))))
      .flatMap(_ => users)

  def users: Task[Vector[DBUser]] = runAsTask(query.result).map(_.toVector)

  def selectUser(userName: UserName): Task[Option[DBUser]] =
    runAsTask(query.filter(_.name === userName).result.headOption)

  def userExists(userName: UserName): Task[Boolean] =
    runAsTask(query.filter(_.name === userName).result.headOption).map(_.isDefined)

  def addUserIfNotExists(userName: UserName, password: Password): Task[Boolean] =
    userExists(userName).flatMap(if (_) false.pure[Task] else addUser(userName, password).map(_ => true))

  def correctPassword(userName: UserName, password: Password): Task[Option[DBUser]] = {
    for {
      maybeUser <- runAsTask(query.filter(_.name === userName).result.headOption)
      userIfPWIsCorrect = maybeUser.filter(user => BCrypt.checkpw(password, user.password))
    } yield userIfPWIsCorrect
  }

  def changePassword(userName: UserName, newPassword: Password): Task[Boolean] =
    for {
      maybeUser <- selectUser(userName)
      hashed <- hashPassword(newPassword).pure[Task]
      updateQuery = maybeUser.map(user => query.filter(_.name === userName).update(user.copy(password = hashed)))
      done <- updateQuery match {
        case Some(update) => runAsTask(update).map(_ => true)
        case None         => false.pure[Task]
      }
    } yield done

}
