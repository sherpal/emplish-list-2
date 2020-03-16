package controllers

import io.circe.generic.auto._
import javax.inject.Inject
import models.DBUser
import models.database.{Registration, Users}
import models.errors.BackendError._
import models.errors.{BackendError, BackendException}
import models.guards.{FullAuthGuardFactory, UserAction}
import models.users.{LoginUser, NewUser, PendingRegistration, User}
import monix.eval.Task
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.HttpErrorHandler
import play.api.mvc._
import slick.jdbc.JdbcProfile
import utils.ReadsImplicits._
import utils.WriteableImplicits._
import utils.database.tables.UsersTable
import utils.database.tables.UsersTable.{Password, UserName}
import utils.mail.InviteEmails
import utils.monix.SchedulerProvider
import utils.playzio.PlayZIO._
import zio.ZIO

import scala.concurrent.{ExecutionContext, Future}

final class UsersController @Inject()(
    assets: Assets,
    errorHandler: HttpErrorHandler,
    config: Configuration,
    authGuard: FullAuthGuardFactory,
    protected val dbConfigProvider: DatabaseConfigProvider,
    cc: ControllerComponents,
    val ec: ExecutionContext
) extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile]
    with SchedulerProvider
    with Users
    with Registration
    with InviteEmails {

  private val usersLayer =
    utils.config.Configuration.live ++ (models.database.dbProvider(db) >>> Users.live)

  def user: Action[AnyContent] = authGuard() { implicit request: UserAction.SessionRequest[AnyContent] =>
    Ok(User(request.userId, request.userName))
  }

  def login: Action[LoginUser] = Action.zio(parse.json[LoginUser]) { request =>
    (for {
      user <- ZIO.succeed(request.body)
      maybeDBUser <- Users.correctPassword(user.name, user.password)
      result = maybeDBUser match {
        case Some(DBUser(id, _, _)) =>
          Ok.withSession(
            models.Global.USER_ID -> id,
            models.Global.TIMESTAMP -> utils.time.Time.epochSecond.toString,
            models.Global.USER_NAME -> user.name
          )
        case None => BadRequest
      }

    } yield result).orDie.provideLayer(usersLayer)
  }

  def logout: Action[AnyContent] = Action { Ok.withNewSession }

  def register: Action[NewUser] = Action.async(parse.json[NewUser]) { implicit request: Request[NewUser] =>
    NewUser.fieldsValidator.validate(request.body) match {
      case m if m.isEmpty =>
        (for {
          //userAdded <- addUserIfNotExists(request.body.name, request.body.password)
          mailPasswordSet <- Task.pure(utils.mail.Mail.password.isDefined)
          _ <- Task { if (!mailPasswordSet) throw new InviteEmails.EmailPasswordNotSet }
          newUser <- Task.pure(request.body)
          hashedPassword = UsersTable.hashPassword(newUser.password)
          randomKey <- registerUser(newUser.name, hashedPassword, newUser.email)
          wasAdded = randomKey.nonEmpty
          emailSent <- if (wasAdded)
            newRegistration(PendingRegistration(newUser.name, newUser.email, hashedPassword, randomKey))
          else Task.pure(Left[BackendError, Boolean](UserNameAlreadyExists(request.body.name)))
          result = emailSent match {
            case Right(_)    => Ok
            case Left(error) => BadRequest(Map("registrationError" -> List(error)))
          }
        } yield result)
          .onErrorHandle(_ => InternalServerError)
          .runToFuture
      case m => Future.successful(BadRequest(m))
    }
  }

  def downloadPendingRegistrations(limit: Int): Action[AnyContent] = authGuard.admin.async { _ =>
    pendingRegistrations(limit).map(Ok(_)).runToFuture
  }

  def resetPassword(userName: UserName): Action[AnyContent] = authGuard().zio { implicit request: Request[AnyContent] =>
    Users.changePassword(userName, "").orDie.map(Ok(_)).provideLayer(usersLayer)
  }

  def changePW(newPassword: Password): Action[AnyContent] = authGuard().zio {
    implicit request: UserAction.SessionRequest[AnyContent] =>
      Users
        .changePassword(request.userName, newPassword)
        .orDie
        .provideLayer(usersLayer)
        .map(Ok(_))
  }

  def amIAdmin: Action[AnyContent] = authGuard.admin(_ => Ok)

  def pendingRegistrationEmail(userName: String, randomKey: String): Action[AnyContent] = authGuard.admin.async {
    implicit request =>
      registrationEmail(userName, randomKey).map {
        case Some(email) => Ok(email)
        case None        => BadRequest("The given userName and randomKey are not in pending registration.")
      }.runToFuture
  }

  def usersNames: Action[AnyContent] = authGuard.admin.zio(
    Users.users.provideLayer(usersLayer).map(_.map(_.name)).orDie.map(Ok(_))
  )

  def addPreRegisteredUser(userName: UserName, randomKey: String): Action[AnyContent] = authGuard.admin.async {
    implicit request =>
      (for {
        userEmail <- registrationEmail(userName, randomKey).map(_.get)
        userAccepted <- acceptUser(userName, randomKey)
        emailSent <- if (userAccepted) registrationAccepted(userName, userEmail)
        else Task.pure(Left[BackendError, Boolean](BackendError("acceptUserFailed", "")))
      } yield emailSent)
        .onErrorRecover {
          case e: BackendException => Left[BackendError, Boolean](e.backendError)
        }
        .map {
          case Left(error) => BadRequest(Map("error" -> List(error)))
          case Right(_)    => Ok
        }
        .runToFuture
  }

  def rejectPreRegisteredUser(userName: UserName, randomKey: String): Action[AnyContent] = authGuard.admin.async {
    implicit request =>
      rejectUser(userName, randomKey).map(Ok(_)).runToFuture
  }

  def deleteUserRoute(userName: UserName): Action[AnyContent] = authGuard.admin.zio {
    Users.deleteNonAdminUser(userName).orDie.map(Ok(_)).provideLayer(usersLayer)
  }

}
