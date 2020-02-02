package models.emplishlist

import models.emplishlist.db.{DBIngredient, DBIngredientsInStore}
import models.errors.BackendError
import models.validators.FieldsValidator
import models.validators.StringValidators._
import models.validators.Validator._
import syntax.WithUnit

final case class Ingredient(id: Int, name: String, unit: IngredientUnit, stores: List[Store], tags: List[String])
    extends Ordered[Ingredient] {
  def toDBIngredient: DBIngredient = DBIngredient(id, name, unit.name, tags.mkString(" "))
  def ingredientsInStore: List[DBIngredientsInStore] = stores.map(s => DBIngredientsInStore(id, s.id))

  def compare(that: Ingredient): Int = this.name compare that.name
}

object Ingredient {

  implicit def ingredientUnit: WithUnit[Ingredient] =
    WithUnit(Ingredient(0, "", implicitly[WithUnit[IngredientUnit]].unit, List(implicitly[WithUnit[Store]].unit), Nil))

  def empty: Ingredient = ingredientUnit.unit

  def withName(name: String): Ingredient = empty.copy(name = name)

  def validator(
      ingredients: Vector[Ingredient],
      units: Vector[IngredientUnit],
      stores: Vector[Store]
  ): FieldsValidator[Ingredient, BackendError] =
    FieldsValidator(
      Map(
        "name" -> (nonEmptyString ++
          simpleValidator(
            (name: String) => !ingredients.exists(_.name == name),
            BackendError("validator.ingredientExists", _)
          ))
          .contraMap[Ingredient](_.name),
        "unit" -> simpleValidator[IngredientUnit, BackendError](
          units.contains,
          unit => BackendError("validator.unitDoesNotExist", unit.name)
        ).contraMap[Ingredient](_.unit),
        "stores" -> (simpleValidator[Store, BackendError](
          stores.contains,
          store => BackendError("validator.storeDoesNotExist", store.name)
        ).contraFlatMap[Ingredient](_.stores) ++ simpleValidator[List[Store], BackendError](
          stores => stores.diff(stores.distinct).isEmpty,
          stores => BackendError("validator.NotAllDifferentStores", stores.diff(stores.distinct).mkString(", "))
        ).contraMap[Ingredient](_.stores)),
        "tags" -> onlyLowercaseLetters.contraFlatMap[Ingredient](_.tags)
      )
    )

}
