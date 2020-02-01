package models.emplishlist.forms

import models.emplishlist.{Ingredient, IngredientQuantity}
import syntax.WithUnit

import scala.collection.immutable
import scala.util.Try

final case class UserEnteredIngredientQuantity(ingredientName: String, amount: String) {

  def ingredientQuantity(ingredients: immutable.Seq[Ingredient]): Option[IngredientQuantity] =
    for {
      amountInDouble <- Try(amount.toDouble).toOption
      ingredient <- ingredients.find(_.name == ingredientName)
    } yield IngredientQuantity(ingredient, amountInDouble)

}

object UserEnteredIngredientQuantity {

  def fromIngredientQuantity(ingredientQuantity: IngredientQuantity): UserEnteredIngredientQuantity =
    UserEnteredIngredientQuantity(
      ingredientQuantity.ingredient.name,
      ingredientQuantity.amount.toString
    )

  implicit def withUnit(
      implicit ingredientUnit: WithUnit[IngredientQuantity]
  ): WithUnit[UserEnteredIngredientQuantity] =
    WithUnit(fromIngredientQuantity(ingredientUnit.unit))

}
