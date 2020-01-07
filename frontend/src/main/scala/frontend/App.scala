package frontend

import frontend.components.login.{Login, Register}
import frontend.components.main.{RedirectHome, Todo}
import frontend.utils.history.DefaultNavigator
import org.scalajs.dom.History
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.reactrouter._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("resources/App.css", JSImport.Default)
@js.native
object AppCSS extends js.Object

@JSImport("resources/logo.svg", JSImport.Default)
@js.native
object ReactLogo extends js.Object

@JSImport("resources/trash.svg", JSImport.Default)
@js.native
object TrashPictogram extends js.Object

@JSImport("resources/plus2.svg", JSImport.Default)
@js.native
object NewItemPictogram extends js.Object

@react class App extends StatelessComponent {
  type Props = Unit

  private val css = AppCSS

  println(css)

  ReactRouterDOM

  val history: History = DefaultNavigator.history

  def render(): ReactElement = Router(history)(
    Route(path = "/", component = RedirectHome, exact = true),
    Route(path = "/home", component = components.main.Main),
    Route(path = "/ingredients", component = components.main.Main),
    Route(path = "/new-ingredient", component = components.main.Main),
    Route(path = "/recipes", component = components.main.Main),
    Route(path = "/basket", component = components.main.Main),
    Route(path = "/handle-registration", component = components.main.Main),
    Route(path = "/login", component = Login),
    Route(path = "/sign-up", component = Register),
    Route(path = "/todo-react", component = Todo)
  )
}
