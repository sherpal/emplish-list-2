package frontend.laminar.components.ingredients

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import io.circe.generic.auto._
import models.emplishlist.{Ingredient, IngredientUnit, Store}
import org.scalajs.dom
import org.scalajs.dom.html
import urldsl.language.QueryParameters.dummyErrorImpl.{param => qParam}

final class NewIngredient(maybeIngredientId: Option[Int])(implicit actorSystemContainer: ActorSystemContainer)
    extends Component[dom.html.Div] {
  import actorSystemContainer._

  val lastAddedIngredient: Var[(Option[Ingredient], Boolean)] = Var((None, false))

  val download = new InfoDownloader("ingredients")

  val element: ReactiveHtmlElement[html.Div] = {
    val $stores = download.downloadInfo[Vector[Store]]("stores")
    val $units = download.downloadInfo[Vector[IngredientUnit]]("units")
    val $ingredients = download.downloadInfo[Vector[Ingredient]]("ingredients")
    val $updatedIngredient = download.downloadInfoWithParams[Option[Ingredient], Int](
      "get-ingredient",
      qParam[Int]("id")
    )(maybeIngredientId.getOrElse(-1)) // -1 will return None

    val $info = $updatedIngredient.combineWith($stores).combineWith($units).combineWith($ingredients).collect {
      case (((Some(maybeIngredient), Some(stores)), Some(units)), Some(ingredients)) =>
        (maybeIngredient, stores, units, ingredients)
    }

    div(
      child <-- lastAddedIngredient.signal.map {
        case (Some(ingredient), true) => div(s"Ingredient ${ingredient.name} was successfully added!")
        case (Some(ingredient), false) => div(s"Ingredient ${ingredient.name} was successfully updated!")
        case (None, _)                => emptyNode
      },
      child <-- $info.map {
        case (maybeIngredient, stores, units, ingredients) =>
          NewIngredientForm(
            maybeIngredient,
            ingredients,
            units,
            stores,
            lastAddedIngredient.writer.contramap[(Ingredient, Boolean)] {
              case (ingredient, isAdded) => (Some(ingredient), isAdded)
            }
          )
      }
    )

  }

}

object NewIngredient {

  def apply(maybeIngredientId: Option[Int])(implicit actorSystemContainer: ActorSystemContainer): NewIngredient =
    new NewIngredient(maybeIngredientId)

}
