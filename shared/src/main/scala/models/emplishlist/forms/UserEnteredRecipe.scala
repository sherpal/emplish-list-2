package models.emplishlist.forms

import models.emplishlist.{Ingredient, IngredientQuantity, Recipe}
import syntax.WithUnit

import scala.annotation.tailrec
import scala.collection.immutable

final case class UserEnteredRecipe(
    uniqueId: Int,
    name: String,
    ingredients: List[UserEnteredIngredientQuantity],
    createdBy: String,
    createdOn: Long,
    lastUpdateBy: String,
    lastUpdateOn: Long,
    description: String,
    forHowManyPeople: Int,
    tags: List[String]
) {

  def maybeRecipe(existingIngredients: immutable.Seq[Ingredient]): Option[Recipe] = {

    @tailrec
    def maybeIngredients(
        remaining: List[UserEnteredIngredientQuantity],
        accumulated: Option[List[IngredientQuantity]]
    ): Option[List[IngredientQuantity]] =
      accumulated match {
        case None => None
        case Some(acc) =>
          remaining match {
            case Nil          => accumulated.map(_.reverse)
            case head :: tail => maybeIngredients(tail, head.ingredientQuantity(existingIngredients).map(_ +: acc))
          }
      }

    maybeIngredients(ingredients, Some(Nil)).map { iqs =>
      Recipe(
        uniqueId,
        name,
        iqs,
        createdBy,
        createdOn,
        lastUpdateBy,
        lastUpdateOn,
        description,
        forHowManyPeople,
        tags
      )
    }
  }

}

object UserEnteredRecipe {

  implicit def withUnit(implicit recipeUnit: WithUnit[Recipe]): WithUnit[UserEnteredRecipe] =
    WithUnit(fromRecipe(recipeUnit.unit))

  def fromRecipe(recipe: Recipe): UserEnteredRecipe = UserEnteredRecipe(
    recipe.uniqueId,
    recipe.name,
    recipe.ingredients.map {
      case IngredientQuantity(ingredient, amount) => UserEnteredIngredientQuantity(ingredient.name, amount.toString)
    },
    recipe.createdBy,
    recipe.createdOn,
    recipe.lastUpdateBy,
    recipe.lastUpdateOn,
    recipe.description,
    recipe.forHowManyPeople,
    recipe.tags
  )

}
