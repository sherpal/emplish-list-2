package models.emplishlist

import models.emplishlist.db.DBRecipeIngredient
import models.errors.BackendError
import models.validators.{FieldsValidator, NumericValidators, Validator}
import syntax.WithUnit

final case class IngredientQuantity(ingredient: Ingredient, amount: Double) extends Ordered[IngredientQuantity] {
  def toDBRecipeIngredient(recipeId: Int): DBRecipeIngredient =
    DBRecipeIngredient(ingredient.id, recipeId, amount)

  def *(x: Double): IngredientQuantity = copy(amount = x * amount)

  def compare(that: IngredientQuantity): Int = this.ingredient compare that.ingredient
}

object IngredientQuantity {

  val tupled: ((Ingredient, Double)) => IngredientQuantity = (apply _).tupled

  implicit def validator(implicit ingredients: List[Ingredient]): FieldsValidator[IngredientQuantity, BackendError] =
    FieldsValidator(
      Map(
        "amount" -> NumericValidators[Double].positive.contraMap[IngredientQuantity](_.amount),
        "ingredient" -> Validator
          .simpleValidator[Ingredient, BackendError](
            ingredients.contains,
            ingredient => BackendError("ingredientDoesNotExist", ingredient.toString)
          )
          .contraMap(_.ingredient)
      )
    )

  implicit def ingredientQuantityUnit(implicit ingredientUnit: WithUnit[Ingredient]): WithUnit[IngredientQuantity] =
    new WithUnit[IngredientQuantity] {
      def unit: IngredientQuantity = IngredientQuantity(ingredientUnit.unit, 0.0)
    }

}
