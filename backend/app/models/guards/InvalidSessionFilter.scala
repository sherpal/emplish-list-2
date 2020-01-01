package models.guards

import io.circe.generic.auto._
import models.errors.BackendError.InvalidSessionError
import play.api.mvc.{ActionFilter, Request, Result, Results}
import utils.WriteableImplicits._

import scala.concurrent.{ExecutionContext, Future}

object InvalidSessionFilter extends Results {

  def apply()(implicit ec: ExecutionContext): ActionFilter[Request] = new ActionFilter[Request] {
    protected def filter[A](request: Request[A]): Future[Option[Result]] =
      Future {
        request.session(models.Global.USER_ID) // checking that user id exists
        request.session(models.Global.TIMESTAMP).toLong // checking that timestamp exists and is a long
        request.session(models.Global.USER_NAME) // checking that user name exists
        Option.empty[Result]
      }.recover {
        case t => Some(Forbidden(InvalidSessionError(t.getMessage)))
      }

    protected def executionContext: ExecutionContext = ec
  }

}
