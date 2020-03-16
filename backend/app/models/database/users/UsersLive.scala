package models.database.users

import models.DBUser
import models.database.Users
import slick.jdbc.JdbcProfile
import utils.database.tables.UsersTable
import utils.database.tables.UsersTable.{Password, UserName}
import zio.{Task, ZIO}

final class UsersLive(
    api: JdbcProfile#API
)(implicit db: JdbcProfile#Backend#Database)
    extends Users.Service {
  import models.database.runAsTask
  import api._

  val query = UsersTable.query

  def addUser(dBUser: DBUser): Task[Int] = runAsTask(query += dBUser)

  def users: Task[Vector[DBUser]] = runAsTask(query.result).map(_.toVector)

  def selectUser(userName: UserName): Task[Option[DBUser]] =
    runAsTask(query.filter(_.name === userName).result.headOption)

  def deleteUser(userName: UserName): Task[Int] =
    runAsTask(query.filter(_.name === userName).delete)

  def userExists(userName: UserName): Task[Boolean] =
    runAsTask(query.filter(_.name === userName).result.headOption).map(_.isDefined)

  def changePassword(userName: UserName, newPassword: Password): Task[Boolean] =
    for {
      maybeUser <- selectUser(userName)
      hashed = UsersTable.hashPassword(newPassword)
      updateQuery = maybeUser.map(user => query.filter(_.name === userName).update(user.copy(password = hashed)))
      done <- updateQuery match {
        case Some(update) => runAsTask(update).map(_ => true)
        case None         => ZIO.succeed(false)
      }
    } yield done

}
