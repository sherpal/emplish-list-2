package models.guards

import models.guards.UserAction.SessionRequest
import play.api.mvc.{ActionFilter, Result, Results}
import utils.config.ConfigRequester.|>

import scala.concurrent.{ExecutionContext, Future}

object AdminSessionFilter extends Results {

  private def adminName = (|> >> "adminUser" >> "name").into[String]

  def apply()(implicit ec: ExecutionContext): ActionFilter[SessionRequest] = new ActionFilter[SessionRequest] {
    protected def filter[A](request: SessionRequest[A]): Future[Option[Result]] =
      if (request.userName == adminName) Future.successful(Option.empty[Result])
      else Future.successful(Some(Forbidden))

    protected def executionContext: ExecutionContext = ec
  }
}
