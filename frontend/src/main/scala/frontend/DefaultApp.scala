package frontend

import akka.actor.ActorSystem
import frontend.forms.FormComponent
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.scalajs.js

@react final class DefaultApp extends StatelessComponent {

  implicit val actorSystem: ActorSystem = ActorSystem("slinky")

  override def componentWillUnmount(): Unit = {
    actorSystem.terminate()
  }

  type Props = Unit

  override def render(): ReactElement = {
    div(
      div(className := "App")(
        header(className := "App-header")(
          img(src := ReactLogo.asInstanceOf[String], className := "App-logo", alt := "logo"),
          h1(className := "App-title")("Welcome to React (with Scala.js!)")
        ),
        p(className := "App-intro")(
          "To get started, edit ",
          code("App.scala"),
          " and save to reload."
        ),
        div(
          "Example of using a button to make an http call to play:",
          MakeCallButton()
        )
      ),
      div(
        style := js.Dynamic.literal(
          marginTop = "30px"
        )
      )(
        FormComponent(actorSystem)
      )
    )
  }

}
