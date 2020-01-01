package models.guards

import javax.inject.Inject
import models.guards.UserAction.SessionRequest
import play.api.mvc._
import utils.database.tables.UsersTable.UserName

import scala.concurrent.{ExecutionContext, Future}

/**
  * Here we can safely get the `USER_ID` and the `TIMESTAMP` and cast them respectively to Int and Long
  * since we will have passed through the [[InvalidSessionFilter]] before using this.
  */
final class UserAction @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[SessionRequest, AnyContent]
    with ActionTransformer[Request, SessionRequest] {
  def transform[A](request: Request[A]): Future[SessionRequest[A]] = Future.successful {
    SessionRequest(
      request.session(models.Global.USER_ID),
      request.session(models.Global.USER_NAME),
      request.session(models.Global.TIMESTAMP).toLong,
      request
    )
  }
}

object UserAction {
  final case class SessionRequest[A](userId: String, userName: UserName, timestamp: Long, request: Request[A])
      extends WrappedRequest[A](request)

}
