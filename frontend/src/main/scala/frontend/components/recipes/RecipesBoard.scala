package frontend.components.recipes

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import frontend.components.helpers.InfoDownloader
import io.circe.generic.auto._
import models.emplishlist.Recipe
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{div, h1}

import scala.concurrent.ExecutionContext

@react final class RecipesBoard extends Component {

  type Props = Unit

  case class State(recipes: Vector[Recipe])

  implicit val actorSystem: ActorSystem = ActorSystem("Recipes")
  implicit val mat: Materializer = ActorMaterializer()
  implicit def ec: ExecutionContext = actorSystem.dispatcher

  def initialState: State = State(Vector())

  def thisState: State = state

  override def componentWillUnmount(): Unit = {
    actorSystem.terminate()
  }

  lazy val downloader: InfoDownloader[State] = InfoDownloader("recipes", setState)

  override def componentWillMount(): Unit = {
    downloader.downloadInfo[Recipe]("recipes", recipes => _.copy(recipes = recipes))
  }

  def render(): ReactElement = div(
    h1("Recipes"),
    RecipeList(state.recipes)
  )

}
