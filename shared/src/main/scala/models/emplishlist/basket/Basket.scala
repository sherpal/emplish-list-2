package models.emplishlist.basket

import models.emplishlist.{IngredientQuantity, RecipeQuantity}
import syntax.WithUnit

final case class Basket(recipes: List[RecipeQuantity], extraIngredients: List[IngredientQuantity]) {

  def allIngredients: List[IngredientQuantity] =
    (recipes.flatMap(_.ingredientQuantities) ++ extraIngredients)
      .groupBy(_.ingredient)
      .mapValues(_.map(_.amount).sum)
      .toList
      .map(IngredientQuantity.tupled)
      .sorted

}

object Basket {

  implicit def basketUnit: WithUnit[Basket] = WithUnit(Basket(Nil, Nil))

  def empty: Basket = basketUnit.unit

}
