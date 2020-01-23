package frontend.laminarcomponents

import com.raquo.laminar.api.L._
import frontend.laminar.components.helpers.InputSearch
import org.scalajs.dom

object Test {

  {
    val d = dom.document.createElement("div")
    dom.document.body.appendChild(d)

    case class User(name: String)

    val users = List(
      User("antoine"),
      User("souad"),
      User("sébastien"),
      User("alice"),
      User("sophie"),
      User("ariane"),
      User("jean-paul")
    )

    val values = Var[User](User(""))

    render(
      d,
      InputSearch(
        values,
        users,
        (user1: User) => (user2: User) => user2.name.toLowerCase.contains(user1.name.toLowerCase),
        User.apply,
        (_: User).name
      )
    )
  }

}
