package frontend

import cats.effect.{ExitCode, IO}
import com.raquo.laminar.nodes.ReactiveRoot
import org.scalajs.dom
import org.scalajs.dom.raw.Element
//import slinky.hot

import scala.scalajs.js.annotation.{JSExportTopLevel, JSImport}
import scala.scalajs.{LinkingInfo, js}

@JSImport("resources/index.css", JSImport.Default)
@js.native
object IndexCSS extends js.Object

object Main {
  val css: IndexCSS.type = IndexCSS

  val initializeHot: IO[Unit] = IO {
    if (LinkingInfo.developmentMode) {
      //hot.initialize()
      println("hot?")
    }
  }

  val createContainer: IO[Element] = IO {
    Option(dom.document.getElementById("root")).getOrElse {
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      elem
    }
  }

  def render(container: dom.Element): IO[ReactiveRoot] = IO {
    com.raquo.laminar.api.L.render(container, laminar.components.App())
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
