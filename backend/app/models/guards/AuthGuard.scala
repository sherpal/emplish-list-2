package models.guards

import models.Global
import play.api.Configuration
import play.api.mvc.Results._
import play.api.mvc._
import utils.time.Time._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

final class AuthGuard(val parser: BodyParsers.Default, config: Configuration)(
    implicit val executionContext: ExecutionContext
) extends ActionBuilder[Request, AnyContent] {

  private val logger = play.api.Logger(this.getClass)
  private val maxAge: Int = config.get[Int]("play.http.session.maxAge") / 1000

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    logger.info("ENTERED AuthGuard::invokeBlock ...")

    (for {
      userId <- request.session.get(Global.USER_ID)
      timestamp <- request.session.get(Global.TIMESTAMP)
      userName <- request.session.get(Global.USER_NAME)
      age <- Try(timestamp.toLong).toOption
      if epochSecond - age < maxAge
    } yield
      block(request).map(
        _.withSession(
          Global.USER_ID -> userId,
          Global.TIMESTAMP -> epochSecond.toString,
          Global.USER_NAME -> userName
        )
      ))
      .getOrElse(Future.successful(Unauthorized))

  }
}
