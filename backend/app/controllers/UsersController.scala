package controllers

import io.circe.generic.auto._
import javax.inject.Inject
import models.DBUser
import models.database.Users
import models.errors.BackendError._
import models.guards.{FullAuthGuardFactory, UserAction}
import models.users.{LoginUser, NewUser, User}
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.HttpErrorHandler
import play.api.mvc._
import slick.jdbc.JdbcProfile
import utils.ReadsImplicits._
import utils.WriteableImplicits._
import utils.database.tables.UsersTable.{Password, UserName}
import utils.monix.Implicits._
import utils.monix.SchedulerProvider

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
    with Users {

  def user: Action[AnyContent] = authGuard() { implicit request: UserAction.SessionRequest[AnyContent] =>
    Ok(User(request.userId, request.userName))
  }

  def login: Action[LoginUser] = Action.async(parse.json[LoginUser]) { implicit request: Request[LoginUser] =>
    val user = request.body
    correctPassword(user.name, user.password).map {
      case Some(DBUser(id, _, _)) =>
        Ok.withSession(
          models.Global.USER_ID -> id,
          models.Global.TIMESTAMP -> utils.time.Time.epochSecond.toString,
          models.Global.USER_NAME -> user.name
        )
      case None => BadRequest
    }.runToFuture
  }

  def logout: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok.withNewSession
  }

  def register: Action[NewUser] = Action.async(parse.json[NewUser]) { implicit request: Request[NewUser] =>
    NewUser.fieldsValidator.validate(request.body) match {
      case m if m.isEmpty =>
        (for {
          userAdded <- addUserIfNotExists(request.body.name, request.body.password)
        } yield
          if (userAdded) Ok
          else BadRequest(Map("name" -> List(UserNameAlreadyExists(request.body.name)))))
          .onErrorHandle(_ => InternalServerError)
          .runToFuture
      case m => Future.successful(BadRequest(m))
    }
  }

  def resetPassword(userName: UserName): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    changePassword(userName, "").runToResult
  }

  def changePW(newPassword: Password): Action[AnyContent] = authGuard().async {
    implicit request: UserAction.SessionRequest[AnyContent] =>
      changePassword(request.userName, newPassword).runToResult
  }

}
