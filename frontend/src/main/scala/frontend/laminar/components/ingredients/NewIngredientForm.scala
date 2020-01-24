package frontend.laminar.components.ingredients

import com.raquo.airstream.eventbus.WriteBus
import com.raquo.laminar.api.L._
import frontend.laminar.utils.ActorSystemContainer
import models.emplishlist.{Ingredient, IngredientUnit, Store}
import models.errors.BackendError

object NewIngredientForm {

  def apply(
      ingredients: Vector[Ingredient],
      units: Vector[IngredientUnit],
      stores: Vector[Store],
      lastIngredientAdded: WriteBus[Ingredient]
  )(
      implicit actorSystemContainer: ActorSystemContainer
  ) = {
    import actorSystemContainer._

    implicit val owner: Owner = new Owner {}

    val ingredient = Var[Ingredient](Ingredient.empty)
    val errors = Var[Map[String, List[BackendError]]](Map())

    val ingredientChangerBus = new EventBus[Ingredient => Ingredient]()
    val nameChanger = ingredientChangerBus.writer.contramapWriter((name: String) => _.copy(name = name))
    val storesChanger = ingredientChangerBus.writer.contramapWriter((stores: List[Store]) => _.copy(stores = stores))
    val unitChanger = ingredientChangerBus.writer.contramapWriter((unit: IngredientUnit) => _.copy(unit = unit))

  }

}
