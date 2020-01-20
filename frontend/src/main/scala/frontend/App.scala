package frontend

import frontend.components.login.{Login, Register}
import frontend.components.main.{RedirectHome, Todo}
import frontend.utils.history.DefaultNavigator
import org.scalajs.dom.History
import router.{Link, Route, Router, Routes}
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import urldsl.errors.DummyError
import urldsl.language.{PathSegment, PathSegmentWithQueryParams}
import urldsl.vocabulary.{PathMatchOutput, Segment, UrlMatching}
import components.main.{Main => MainDashboard}
import org.scalajs.dom
import slinky.web.html._
import urldsl.url.UrlStringParserGenerator

import scala.language.implicitConversions
//import slinky.reactrouter._

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

  val history: History = DefaultNavigator.history

//  def render(): ReactElement = Router(history)(
//    Route(path = "/", component = RedirectHome, exact = true),
//    Route(path = "/home", component = components.main.Main),
//    Route(path = "/ingredients", component = components.main.Main),
//    Route(path = "/new-ingredient", component = components.main.Main),
//    Route(path = "/recipes", component = components.main.Main),
//    Route(path = "/basket", component = components.main.Main),
//    Route(path = "/handle-registration", component = components.main.Main),
//    Route(path = "/login", component = Login),
//    Route(path = "/sign-up", component = Register),
//    Route(path = "/todo-react", component = Todo)
//  )

  import urldsl.language.PathSegment.dummyErrorImpl._
  import urldsl.language.QueryParameters.dummyErrorImpl.{empty => $}

  val debugRoot: PathSegment[Unit, DummyError] = PathSegment.factory[Unit, DummyError](
    (ss: List[Segment]) => {
      println(ss)
      Right(PathMatchOutput((), ss))
    },
    (_: Unit) => Nil
  )

  private implicit def ps[T](
      pathSegment: PathSegment[T, DummyError]
  ): PathSegmentWithQueryParams[T, DummyError, Unit, DummyError] =
    pathSegment ? $

  def render(): ReactElement = div(
    Routes(
      Router.router,
      List(
        Route(debugRoot / endOfSegments, () => RedirectHome()),
        Route(root / "home", () => MainDashboard()),
        Route(root / "ingredients", () => MainDashboard()),
        Route(root / "new-ingredient", () => MainDashboard()),
        Route(root / "recipes", () => MainDashboard()),
        Route(root / "basket", () => MainDashboard()),
        Route(root / "handle-registration", () => MainDashboard()),
        Route(root / "login", () => Login()),
        Route(root / "sign-up", () => Register()),
        Route(
          root,
          () =>
            div(
              h1("404 Not found"),
              h2("You probably overcooked something."),
              p("Don't worry, we'll help you: click ", Link(to = "/", text = "here"))
            )
        )
      )
    )
  )

}
