package router

import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement

@react final class Routes extends Component {

  case class Props(router: Router, routes: List[Route[_, _]])

  case class State(url: String)

  def initialState: State = State(dom.window.location.href)

  lazy val uuid: String = java.util.UUID.randomUUID().toString

  override def componentDidMount(): Unit = {
    props.router.subscribe(uuid, {
      case (url: String, _) =>
        println("coucou: " + url)
        setState(_.copy(url = url))
    })
  }

  override def componentWillUnmount(): Unit = {
    props.router.unsubscribe(uuid)
  }

  def render(): ReactElement = {
    println(state.url)
    props.routes.flatMap(_.render(state.url)).headOption // todo: headOption ? or...
  }

}
