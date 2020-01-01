package models.emplishlist

import syntax.WithUnit

final case class RecipeQuantity(recipe: Recipe, numberOfPeople: Int) {

  def ingredientQuantities: List[IngredientQuantity] = {
    val multiplier = numberOfPeople.toDouble / recipe.forHowManyPeople

    recipe.ingredients.map(_ * multiplier)
  }

}

object RecipeQuantity {

  implicit def withUnit(implicit recipeUnit: WithUnit[Recipe]): WithUnit[RecipeQuantity] =
    WithUnit(RecipeQuantity(recipeUnit.unit, recipeUnit.unit.forHowManyPeople))

}
