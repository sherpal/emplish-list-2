package models.guards

import javax.inject.{Inject, Singleton}
import models.guards.UserAction.SessionRequest
import play.api.Configuration
import play.api.mvc.{ActionBuilder, AnyContent, BodyParsers}

import scala.concurrent.ExecutionContext

@Singleton
final class FullAuthGuardFactory @Inject()(parser: BodyParsers.Default, config: Configuration)(
    implicit ec: ExecutionContext
) {

  private def guard(authGuard: AuthGuard) =
    authGuard.andThen(InvalidSessionFilter()).andThen(new UserAction(parser))

  def apply(): ActionBuilder[SessionRequest, AnyContent] = guard(new AuthGuard(parser, config))

  def admin: ActionBuilder[SessionRequest, AnyContent] = apply().andThen(AdminSessionFilter())

}
