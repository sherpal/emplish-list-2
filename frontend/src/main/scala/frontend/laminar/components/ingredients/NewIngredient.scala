package frontend.laminar.components.ingredients

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import io.circe.generic.auto._
import models.emplishlist.{Ingredient, IngredientUnit, Store}
import org.scalajs.dom
import org.scalajs.dom.html

final class NewIngredient()(implicit actorSystemContainer: ActorSystemContainer) extends Component[dom.html.Div] {
  import actorSystemContainer._

  val lastAddedIngredient: Var[Option[Ingredient]] = Var(None)

  val download = new InfoDownloader("ingredients")

  val element: ReactiveHtmlElement[html.Div] = {
    val $stores = download.downloadInfo[Vector[Store]]("stores")
    val $units = download.downloadInfo[Vector[IngredientUnit]]("units")
    val $ingredients = download.downloadInfo[Vector[Ingredient]]("ingredients")

    val $info = $stores.combineWith($units).combineWith($ingredients).collect {
      case ((Some(stores), Some(units)), Some(ingredients)) => (stores, units, ingredients)
    }

    div(
      child <-- lastAddedIngredient.signal.map {
        case Some(ingredient) => div(s"Ingredient ${ingredient.name} was successfully added!")
        case None             => div()
      },
      child <-- $info.map {
        case (stores, units, ingredients) =>
          NewIngredientForm(ingredients, units, stores, lastAddedIngredient.writer.contramap[Ingredient](Some(_)))
      }
    )

  }

}

object NewIngredient {

  def apply()(implicit actorSystemContainer: ActorSystemContainer): NewIngredient = new NewIngredient()

}
