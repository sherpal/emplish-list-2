package frontend

import frontend.utils.http.DefaultHttp._
import slinky.core._
import slinky.core.annotations.react
import slinky.web.html._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success}

@react final class MakeCallButton extends Component {

  type Props = Unit
  type State = String

  def initialState: String = "empty"

  def makeRequest(): Unit = {
    boilerplate
      .get(path("hello", s"${Random.nextInt()}"))
      .send()
      .onComplete {
        case Success(value)     => setState(value.toString)
        case Failure(exception) => setState(exception.getMessage)
      }
  }

  def render() = p(
    button(
      onClick := (() => makeRequest())
    )("Click me!"),
    "response: ",
    state
  )

}
