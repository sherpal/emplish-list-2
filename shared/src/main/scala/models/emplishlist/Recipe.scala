package models.emplishlist

import models.emplishlist.db.{DBRecipe, DBRecipeInfo}
import models.errors.BackendError
import models.validators.StringValidators._
import models.validators.{FieldsValidator, NumericValidators, Validator}
import syntax.WithUnit

final case class Recipe(
    uniqueId: Int,
    name: String,
    ingredients: List[IngredientQuantity],
    createdBy: String,
    createdOn: Long,
    lastUpdateBy: String,
    lastUpdateOn: Long,
    description: String,
    forHowManyPeople: Int
) {
  def toDBRecipeInfo: DBRecipeInfo = DBRecipeInfo(
    DBRecipe(uniqueId, name, createdBy, createdOn, lastUpdateBy, lastUpdateOn, description, forHowManyPeople),
    ingredients.map(_.toDBRecipeIngredient(uniqueId))
  )
}

object Recipe {

  implicit def recipeWithUnit: WithUnit[Recipe] = WithUnit(Recipe(0, "", Nil, "", 0, "", 0, "", 0))

  def empty: Recipe = recipeWithUnit.unit

  def withName(name: String): Recipe = empty.copy(name = name)

  def validator(
      implicit ingredientQuantityValidator: FieldsValidator[IngredientQuantity, BackendError],
      ingredients: List[Ingredient]
  ): FieldsValidator[Recipe, BackendError] =
    FieldsValidator(
      Map(
        "name" -> nonEmptyString.contraMap[Recipe](_.name),
        "ingredients" -> Validator
          .simpleValidator((_: Recipe).ingredients.nonEmpty, (_: Recipe) => BackendError("emptyIngredientList", "")),
        "nbrPeople" -> NumericValidators[Int].positive.contraMap[Recipe](_.forHowManyPeople)
      ) ++ ingredientQuantityValidator.contraFlatMap[Recipe](_.ingredients).fields
    )

}
