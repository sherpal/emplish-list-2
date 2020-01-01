package frontend.components.ingredients

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import frontend.components.helpers.InfoDownloader
import io.circe.generic.auto._
import models.emplishlist.{Ingredient, IngredientUnit, Store}
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.concurrent.ExecutionContext

@react final class IngredientsBoard extends Component {

  type Props = Unit

  implicit val actorSystem: ActorSystem = ActorSystem("Ingredients")
  implicit val mat: Materializer = ActorMaterializer()
  implicit def ec: ExecutionContext = actorSystem.dispatcher

  case class State(ingredients: Vector[Ingredient], units: Vector[IngredientUnit], stores: Vector[Store])

  def initialState: State = State(Vector(), Vector(), Vector())

  def thisState: State = state

  override def componentWillUnmount(): Unit = {
    actorSystem.terminate()
  }

  lazy val downloader: InfoDownloader[State] = InfoDownloader("ingredients", setState)

  override def componentWillMount(): Unit = {
    downloader.downloadInfo[Ingredient]("ingredients", ingredients => _.copy(ingredients = ingredients))
    downloader.downloadInfo[IngredientUnit]("units", units => _.copy(units = units))
    downloader.downloadInfo[Store]("stores", stores => _.copy(stores = stores))
  }

  def render(): ReactElement = div(
    IngredientList(state.ingredients),
    StoreList(state.stores),
    UnitList(state.units)
  )

}
