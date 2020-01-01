package frontend.components.ingredients

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import frontend.components.helpers.InfoDownloader
import io.circe.generic.auto._
import models.emplishlist.{Ingredient, IngredientUnit, Store}
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class NewIngredient extends Component {

  type Props = Unit

  case class State(
      ingredients: Vector[Ingredient],
      units: Vector[IngredientUnit],
      stores: Vector[Store],
      lastAddedIngredient: Option[Ingredient] = None
  )

  def initialState: State = State(Vector(), Vector(), Vector())

  implicit lazy val actorSystem: ActorSystem = ActorSystem("NewIngredient")
  implicit lazy val mat: Materializer = ActorMaterializer()
  import actorSystem.dispatcher

  override def componentWillUnmount(): Unit = {
    actorSystem.terminate()
  }

  lazy val downloader: InfoDownloader[State] = InfoDownloader("ingredients", setState)

  override def componentWillMount(): Unit = {
    downloader.downloadInfo[Ingredient]("ingredients", ingredients => _.copy(ingredients = ingredients))
    downloader.downloadInfo[IngredientUnit]("units", units => _.copy(units = units))
    downloader.downloadInfo[Store]("stores", stores => _.copy(stores = stores))
  }

  def ingredientAdded(ingredient: Ingredient): Unit =
    setState(_.copy(lastAddedIngredient = Some(ingredient)))

  def render(): ReactElement =
    if (state.stores.isEmpty || state.units.isEmpty) div()
    else
      div(
        state.lastAddedIngredient.map(i => s"${i.name} was successfully added!"),
        NewIngredientForm(
          actorSystem,
          mat,
          state.ingredients,
          state.units,
          state.stores,
          ingredientAdded
        )
      )

}
