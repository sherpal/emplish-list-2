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

  implicit def withUnit(
      implicit ingredientUnit: WithUnit[IngredientQuantity]
  ): WithUnit[UserEnteredIngredientQuantity] =
    WithUnit(UserEnteredIngredientQuantity(ingredientUnit.unit.ingredient.name, ingredientUnit.unit.amount.toString))

}
