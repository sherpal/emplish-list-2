package router

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class Link extends StatelessComponent {

  case class Props(to: String, text: String)

  def render(): ReactElement =
    span(className := "clickable", onClick := (() => Router.router.moveTo(props.to)), props.text)

}
