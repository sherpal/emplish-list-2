package frontend

import cats.effect.{ExitCode, IO}
import org.scalajs.dom
import slinky.hot
import slinky.web.ReactDOM

import scala.scalajs.js.annotation.{JSExportTopLevel, JSImport}
import scala.scalajs.{LinkingInfo, js}

@JSImport("resources/index.css", JSImport.Default)
@js.native
object IndexCSS extends js.Object

object Main {
  val css: IndexCSS.type = IndexCSS

  val initializeHot = IO {
    if (LinkingInfo.developmentMode) {
      hot.initialize()
    }
  }

  val createContainer = IO {
    Option(dom.document.getElementById("root")).getOrElse {
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      elem
    }
  }

  def render(container: dom.Element) = IO {
    ReactDOM.render(App(), container)
  }

  val program: IO[ExitCode] =
    for {
      _ <- initializeHot
      container <- createContainer
      _ <- render(container)
    } yield ExitCode.Success

  @JSExportTopLevel("main")
  def main(): Unit = {
    program.unsafeRunSync()
  }
}
