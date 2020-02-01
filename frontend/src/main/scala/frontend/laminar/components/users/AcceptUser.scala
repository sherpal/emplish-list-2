package frontend.laminar.components.users

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.router.Router
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import models.users.PendingRegistration
import org.scalajs.dom
import org.scalajs.dom.html.Div
import io.circe.generic.auto._
import urldsl.errors.DummyError
import urldsl.language.{PathSegment, PathSegmentWithQueryParams, QueryParameters}
import urldsl.language.QueryParameters.dummyErrorImpl.{param => qParam}
import urldsl.language.PathSegment.dummyErrorImpl._
import frontend.utils.http.DefaultHttp._
import sttp.client.ignore

import scala.util.{Failure, Success}

final class AcceptUser private (maybeUserNameAndRandomKey: Option[(String, String)])(
    implicit actorSystemContainer: ActorSystemContainer
) extends Component[dom.html.Div] {
  import actorSystemContainer._

  val downloader = new InfoDownloader("registration")

  val queryParamForUser: QueryParameters[(String, String), _] = qParam[String]("userName") & qParam[String]("randomKey")

  val pathToHere: PathSegment[Unit, DummyError] = root / "handle-registration"

  val pathToHereWithParams: PathSegmentWithQueryParams[Unit, _, (String, String), _] =
    pathToHere ? queryParamForUser

  def acceptUser(userName: String, randomKey: String): Unit = {
    boilerplate
      .post(
        pathWithMultipleParams(queryParamForUser.createParams(userName, randomKey).mapValues(_.content), "accept-user")
      )
      .response(ignore)
      .send()
      .map(_.is200)
      .onComplete {
        case Success(true) => Router.router.moveTo("/" + pathToHere.createPath())
        case Success(false) =>
          dom.console.warn("That's weird received non 200 from server.")
        case Failure(exception) =>
          throw exception
      }
  }

  def rejectUser(userName: String, randomKey: String): Unit = {
    boilerplate
      .post(
        pathWithMultipleParams(queryParamForUser.createParams(userName, randomKey).mapValues(_.content), "reject-user")
      )
      .response(ignore)
      .send()
      .map(_.is200)
      .onComplete {
        case Success(true) => Router.router.moveTo("/" + pathToHere.createPath())
        case Success(false) =>
          dom.console.warn("That's weird received non 200 from server.")
        case Failure(exception) =>
          throw exception
      }

  }

  val element: ReactiveHtmlElement[Div] = {

    val $usersNames = new InfoDownloader("users").downloadInfo[List[String]]("all-users-names")

    val $pendingRegistrations = downloader.downloadInfo[List[PendingRegistration]]("pending-registrations")

    val $maybeEmail: Observable[Option[String]] = maybeUserNameAndRandomKey
      .map {
        case (userName, randomKey) =>
          downloader.downloadInfoWithParams("registration-email", queryParamForUser)((userName, randomKey))
      }
      .getOrElse(Val(None))

    val maybeRegistrationInfo = $maybeEmail.map { maybeEmail =>
      for {
        email <- maybeEmail
        (userName, randomKey) <- maybeUserNameAndRandomKey
      } yield (userName, randomKey, email)
    }

    div(
      h1("Registrations"),
      child <-- maybeRegistrationInfo
        .map(_.map {
          case (userName, randomKey, email) =>
            section(
              h2("New registration"),
              p(
                s"$userName ($email) has requested access to the Emplish List application",
                br(),
                "Do you want to grant them access to the app?"
              ),
              p(
                button("Accept", onClick.mapTo(()) --> (_ => acceptUser(userName, randomKey))),
                button("Reject", onClick.mapTo(()) --> (_ => rejectUser(userName, randomKey)))
              )
            )
        })
        .map(_.getOrElse(emptyNode)),
      section(
        h2("Pending registrations"),
        table(
          display <-- $pendingRegistrations.map {
            case Some(_ :: Nil) => "block"
            case _              => "none"
          },
          thead(tr(th("User name"), th("email"), th("key"))),
          tbody(
            children <-- $pendingRegistrations.collect { case Some(registrations) => registrations }
              .map(_.map {
                case PendingRegistration(name, email, _, randomKey) =>
                  tr(
                    td(
                      className := "clickable",
                      onClick
                        .mapTo(()) --> (
                          _ => Router.router.moveTo("/" + pathToHereWithParams.createUrlString((), (name, randomKey)))
                      ),
                      name
                    ),
                    td(email),
                    td(abbr(title := randomKey, randomKey.take(7) + "(...)"))
                  )
              })
          )
        ),
        child <-- $pendingRegistrations.filter {
          case Some(Nil) => true
          case None      => true
          case _         => false
        }.mapTo("There are no pending registrations.")
      ),
      section(
        h2("Current users"),
        ul(children <-- $usersNames.collect { case Some(usersNames) => usersNames }.map(_.map(li(_))))
      )
    )
  }

}

object AcceptUser {
  def apply(maybeUsername: Option[String], maybeRandomKey: Option[String])(
      implicit actorSystemContainer: ActorSystemContainer
  ): AcceptUser =
    new AcceptUser(for {
      username <- maybeUsername
      randomKey <- maybeRandomKey
    } yield (username, randomKey))
}
