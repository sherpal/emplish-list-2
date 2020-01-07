package frontend.components.users

import java.net.URLDecoder

import frontend.components.helpers.tables.Table
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.users.PendingRegistration
import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import sttp.client._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@react final class AcceptUser extends Component {

  type Props = Unit

  case class State(
      adminVerified: Boolean = false,
      registrationKey: Option[String] = None,
      registrationUserName: Option[String] = None,
      email: Option[String] = None,
      pendingRegistrationsToDisplay: Option[List[PendingRegistration]] = None
  )

  def initialState: State = State()

  def moveToHome(): Unit = dom.window.location.href = "/home"

  def moveToHere(): Unit = dom.window.location.href = "/handle-registration"

  override def componentWillMount(): Unit = {
    boilerplate.get(path("am-i-admin")).response(ignore).send().map(_.is200).onComplete {
      case Success(true) =>
        setState(_.copy(adminVerified = true))
      case _ =>
        moveToHome()
    }
  }

  override def componentDidMount(): Unit = {
    boilerplate
      .get(path("pending-registrations"))
      .response(responseAs[List[PendingRegistration]])
      .send()
      .map(_.body)
      .onComplete {
        case Success(Right(Right(pendingRegistrations))) =>
          setState(_.copy(pendingRegistrationsToDisplay = Some(pendingRegistrations)))
        case _ =>
      }

    try {

      val queryParams = dom.window.location.search

      val paramsWithoutQuestionMark = queryParams match {
        case "" => ""
        case _  => queryParams.tail
      }
      val keyPairsArguments = paramsWithoutQuestionMark
        .split("&")
        .filter(_.nonEmpty)
        .map(_.split("="))
        .map(_.map(URLDecoder.decode(_, "utf-8")))
        .map(arr => arr(0) -> arr(1))
        .groupBy(_._1)
        .mapValues(_.map(_._2).toList)
      val maybeUserName = keyPairsArguments.get("userName").flatMap(_.headOption)
      val maybeRandomKey = keyPairsArguments.get("randomKey").flatMap(_.headOption)

      setState(_.copy(registrationUserName = maybeUserName, registrationKey = maybeRandomKey))

      for {
        userName <- maybeUserName
        randomKey <- maybeRandomKey
      } yield
        boilerplate
          .get(
            pathWithMultipleParams(queryParamForUser(userName, randomKey), "registration-email")
          )
          .response(asStringAlways)
          .send()
          .filter(_.is200)
          .map(_.body)
          .onComplete {
            case Success(email) => setState(_.copy(email = Some(email)))
            case Failure(t) =>
              throw t
          }

    } catch {
      case t: Throwable =>
        dom.window.alert(t.getMessage + "\n" + t.getStackTrace.mkString("\n\t"))
        moveToHome()
    }

  }

  def queryParamForUser(userName: String, randomKey: String): Map[String, List[String]] = Map(
    "userName" -> List(userName),
    "randomKey" -> List(randomKey)
  )

  def acceptUser(userName: String, randomKey: String): Unit = {
    boilerplate
      .post(pathWithMultipleParams(queryParamForUser(userName, randomKey), "accept-user"))
      .response(ignore)
      .send()
      .map(_.is200)
      .onComplete {
        case Success(true) => moveToHere()
        case Success(false) =>
          dom.console.warn("That's weird received non 200 from server.")
        case Failure(exception) =>
          throw exception
      }
  }

  def rejectUser(userName: String, randomKey: String): Unit = {
    boilerplate
      .post(pathWithMultipleParams(queryParamForUser(userName, randomKey), "reject-user"))
      .response(ignore)
      .send()
      .map(_.is200)
      .onComplete {
        case Success(true) => moveToHere()
        case Success(false) =>
          dom.console.warn("That's weird received non 200 from server.")
        case Failure(exception) =>
          throw exception
      }
  }

  def render(): ReactElement =
    if (state.adminVerified)
      div(
        (state.registrationUserName, state.registrationKey, state.email) match {
          case (Some(userName), Some(randomKey), Some(email)) =>
            div(
              h1("New registration"),
              p(
                s"$userName ($email) has requested access to the emplish list application.",
                br(),
                "Do you want to grant them access to the app?"
              ),
              p(
                button("Accept", onClick := (() => acceptUser(userName, randomKey))),
                button("Reject", onClick := (() => rejectUser(userName, randomKey)))
              )
            )
          case _ => div()
        },
        state.pendingRegistrationsToDisplay.map { pendingRegistrations =>
          div(
            Table[PendingRegistration](
              pendingRegistrations,
              Some(
                tr(
                  th("User name"),
                  th("email"),
                  th("key")
                )
              ),
              (pendingRegistration: PendingRegistration, k: String) =>
                tr(key := k)(
                  td(
                    pendingRegistration.name,
                    className := "clickable",
                    onClick := (
                        () =>
                          dom.document.location.href =
                            s"/handle-registration?userName=${pendingRegistration.name}&randomKey=${pendingRegistration.randomKey}"
                      )
                  ),
                  td(pendingRegistration.email),
                  td(pendingRegistration.randomKey)
                ),
              _.name
            ),
            Some(
              "There are no pending registrations."
            ).filter(_ => pendingRegistrations.isEmpty)
          )
        }
      )
    else div("Verifying authorization level...")

}
