package models.database

import java.util.UUID

import cats.syntax.applicative._
import models.DBUser
import models.database.users.UsersLive
import monix.eval.Task
import org.mindrot.jbcrypt.BCrypt
import slick.jdbc.{JdbcBackend, JdbcProfile}
import utils.config.ConfigRequester.|>
import utils.config.Configuration
import utils.database.DBProfile
import utils.database.DBProfile.api._
import utils.database.tables.UsersTable
import utils.database.tables.UsersTable.{Password, UserName, hashPassword, query}
import zio.{Has, ZIO, ZLayer}

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

  def addUserPasswordHashed(userName: UserName, hashedPassword: Password): Task[Int] =
    randomUUID
      .flatMap(uuid => addUser(DBUser(uuid, userName, hashedPassword)))

  def addUser(userName: UserName, password: Password): Task[Vector[DBUser]] =
    addUserPasswordHashed(userName, hashedPassword = UsersTable.hashPassword(password))
      .flatMap(_ => users)

  def users: Task[Vector[DBUser]] = runAsTask(query.result).map(_.toVector)

  def selectUser(userName: UserName): Task[Option[DBUser]] =
    runAsTask(query.filter(_.name === userName).result.headOption)

  def deleteUser(userName: UserName): Task[Int] =
    runAsTask(query.filter(_.name === userName).delete)

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

  def registerAdminIfNotExist: Task[Boolean] =
    for {
      userName <- Task.pure(|> >> "adminUser" >> "name").map(_.into[String])
      adminExists <- userExists(userName)
      password <- if (!adminExists) Task.pure(|> >> "adminUser" >> "password").map(_.into[String]) else Task.pure("")
      hashedPassword = if (!adminExists) hashPassword(password) else ""
      success <- if (!adminExists) addUserPasswordHashed(userName, hashedPassword) else Task.pure(1)
    } yield success > 0

}

object Users {

  trait Service {

    def addUser(dBUser: DBUser): zio.Task[Int]

    final def addUserPasswordHashed(userName: UserName, hashedPassword: Password): zio.Task[Int] =
      addUser(DBUser(UUID.randomUUID().toString, userName, hashedPassword))

    final def addUser(userName: UserName, password: Password): zio.Task[Vector[DBUser]] =
      addUserPasswordHashed(userName, hashedPassword = UsersTable.hashPassword(password))
        .flatMap(_ => users)

    def users: zio.Task[Vector[DBUser]]

    def selectUser(userName: UserName): zio.Task[Option[DBUser]]

    def deleteUser(userName: UserName): zio.Task[Int]

    def userExists(userName: UserName): zio.Task[Boolean]

    final def addUserIfNotExists(userName: UserName, password: Password): zio.Task[Boolean] =
      for {
        exists <- userExists(userName)
        added <- if (exists) ZIO.succeed(false) else addUser(userName, password).map(_ => true)
      } yield added

    final def correctPassword(userName: UserName, password: Password): zio.Task[Option[DBUser]] =
      for {
        maybeUser <- selectUser(userName)
        userIfPWIsCorrect = maybeUser.filter(user => BCrypt.checkpw(password, user.password))
      } yield userIfPWIsCorrect

    def changePassword(userName: UserName, newPassword: Password): zio.Task[Boolean]

    final def deleteNonAdminUser(userName: UserName): ZIO[Configuration, Throwable, Int] =
      for {
        admin <- Configuration.adminName
        shouldDelete = userName != admin
        deleted <- if (shouldDelete) deleteUser(userName) else ZIO.succeed(0)
      } yield deleted

    final def registerAdminIfNotExist: ZIO[Configuration, Throwable, Boolean] =
      for {
        admin <- Configuration.adminName
        adminExistsFiber <- userExists(admin).fork
        password <- Configuration.adminPassword
        hashedPassword = UsersTable.hashPassword(password)
        adminExists <- adminExistsFiber.join
        success <- if (!adminExists) addUserPasswordHashed(admin, hashedPassword) else ZIO.succeed(1)
      } yield success > 0

  }

  type Users = Has[Users.Service]

  val live: ZLayer[DBProvider, Nothing, Has[Users.Service]] = ZLayer.fromFunction { services: DBProvider =>
    implicit val db: JdbcBackend#DatabaseDef = (services: DBProvider).get[JdbcProfile#Backend#Database]
    new UsersLive(DBProfile.api)
  }

  def addUser(dBUser: DBUser): ZIO[Users, Throwable, Int] =
    ZIO.accessM(_.get.addUser(dBUser))

  def addUserPasswordHashed(userName: UserName, hashedPassword: Password): ZIO[Users, Throwable, Int] =
    ZIO.accessM(_.get.addUserPasswordHashed(userName, hashedPassword))

  def addUser(userName: UserName, password: Password): ZIO[Users, Throwable, Vector[DBUser]] =
    ZIO.accessM(_.get.addUser(userName, password))

  def users: ZIO[Users, Throwable, Vector[DBUser]] = ZIO.accessM(_.get.users)

  def selectUser(userName: UserName): ZIO[Users, Throwable, Option[DBUser]] =
    ZIO.accessM(_.get.selectUser(userName))

  def deleteUser(userName: UserName): ZIO[Users, Throwable, Int] =
    ZIO.accessM(_.get.deleteUser(userName))

  def userExists(userName: UserName): ZIO[Users, Throwable, Boolean] =
    ZIO.accessM(_.get.userExists(userName))

  def addUserIfNotExists(userName: UserName, password: Password): ZIO[Users, Throwable, Boolean] =
    ZIO.accessM(_.get.addUserIfNotExists(userName, password))

  def correctPassword(userName: UserName, password: Password): ZIO[Users, Throwable, Option[DBUser]] =
    ZIO.accessM(_.get.correctPassword(userName, password))

  def changePassword(userName: UserName, newPassword: Password): ZIO[Users, Throwable, Boolean] =
    ZIO.accessM(_.get.changePassword(userName, newPassword))

  def deleteNonAdminUser(userName: UserName): ZIO[Users with Configuration, Throwable, Int] =
    ZIO.accessM(_.get[Users.Service].deleteNonAdminUser(userName))

  def registerAdminIfNotExist: ZIO[Users with Configuration, Throwable, Boolean] =
    ZIO.accessM(_.get[Users.Service].registerAdminIfNotExist)

}
