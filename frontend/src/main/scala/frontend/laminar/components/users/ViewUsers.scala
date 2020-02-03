package frontend.laminar.components.users

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.TrashPictogram
import frontend.laminar.components.Component
import frontend.laminar.router.Router
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import org.scalajs.dom
import org.scalajs.dom.html.Div
import frontend.utils.http.DefaultHttp._
import protocole.Users
import sttp.client.ignore
import urldsl.language.QueryParameters.dummyErrorImpl.{param => qParam}

final class ViewUsers private ()(implicit actorSystemContainer: ActorSystemContainer) extends Component[dom.html.Div] {
  import actorSystemContainer._

  val $usersNames: EventStream[List[String]] = new InfoDownloader("users")
    .downloadInfo[List[String]]("all-users-names")
    .collect { case Some(list) => list }

  val selectedUser: EventBus[Option[String]] = new EventBus()
  val $selectedUser: Signal[Option[String]] = selectedUser.events.fold(
    Option.empty[String]
  )(
    (maybePrevious, maybeNext) =>
      (maybePrevious, maybeNext) match {
        case (Some(previous), Some(next)) if previous != next => maybeNext // toggle selected
        case (Some(previous), Some(next)) if previous == next => None
        case (None, None)                                     => None
        case (None, Some(_))                                  => maybeNext
      }
  )

  def removeUser(userName: String): Unit = {
    boilerplate
      .post(pathWithMultipleParams(Users.userName.createParamsMap(userName), "delete-user"))
      .response(ignore)
      .send()
      .onComplete { _ =>
        Router.router.moveTo("/view-users")
      }
  }

  val element: ReactiveHtmlElement[Div] = div(
    section(
      h2("Current users"),
      ul(
        children <-- $usersNames.map(
          _.map(
            name =>
              li(
                className <-- $selectedUser.map {
                  case Some(user) if user == name => "selected"
                  case _                          => ""
                },
                name,
                span(
                  className := "clickable",
                  onClick.mapTo(Some(name)) --> selectedUser.writer,
                  img(src := TrashPictogram.asInstanceOf[String], alt := "delete", className := "icon-size")
                )
              )
          )
        )
      ),
      child <-- $selectedUser.map {
        case Some(selected) =>
          section(
            s"Remove $selected from emplish list users?",
            button(onClick.mapTo(selected) --> removeUser _, "Remove")
          )
        case None => emptyNode
      }
    )
  )
}

object ViewUsers {
  def apply()(implicit actorSystemContainer: ActorSystemContainer): ViewUsers = new ViewUsers
}
