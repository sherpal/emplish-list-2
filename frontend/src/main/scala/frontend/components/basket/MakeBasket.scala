package frontend.components.basket

import frontend.components.helpers.forms.{InputFromString, InputInt}
import frontend.components.helpers.inputsearch.InputSearch
import frontend.components.helpers.listform.CustomListForm
import models.emplishlist.basket.Basket
import models.emplishlist.{Ingredient, IngredientQuantity, Recipe, RecipeQuantity}
import models.validators.NumericValidators
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import syntax.FromString
import cats.implicits._

@react final class MakeBasket extends Component {

  case class Props(
      currentBasket: Basket,
      changeBasket: Basket => Unit,
      existingRecipes: List[Recipe],
      existingIngredients: List[Ingredient],
      finish: () => Unit
  )

  def existingRecipes: List[Recipe] = props.existingRecipes
  implicit def existingIngredients: List[Ingredient] = props.existingIngredients

  case class State(recipeListIndices: List[Int], customIngredientIndices: List[Int], rawAmounts: List[String])

  def initialState: State = State(
    props.currentBasket.recipes.indices.toList,
    props.currentBasket.extraIngredients.indices.toList,
    props.currentBasket.extraIngredients.map(_.amount.toString)
  )

  private def recipeFromString: FromString[Recipe] = FromString(
    name => existingRecipes.find(_.name == name).get
  )

  private def ingredientFromString: FromString[Ingredient] = FromString(
    name => existingIngredients.find(_.name == name).get
  )

  def recipeRow: CustomListForm.Row[RecipeQuantity] =
    (index: Int, t: RecipeQuantity, onUpdate: (Int, RecipeQuantity) => Unit) =>
      span(
        InputSearch(
          t.recipe.name,
          existingRecipes.map(_.name),
          (entered: String) => (name: String) => name.toLowerCase.contains(entered.toLowerCase),
          (name: String) =>
            onUpdate(index, t.copy(recipe = recipeFromString.optionFromString(name).getOrElse(Recipe.withName(name)))),
          Some((name: String) => existingRecipes.map(_.name).contains(name))
        ),
        " for ",
        InputInt(
          "",
          t.numberOfPeople,
          (newNbrOfPeople: Int) => onUpdate(index, t.copy(numberOfPeople = newNbrOfPeople))
        ),
        " person(s)"
      )

  def customIngredientRow: CustomListForm.Row[(IngredientQuantity, String)] =
    (index: Int, t: (IngredientQuantity, String), onUpdate: (Int, (IngredientQuantity, String)) => Unit) =>
      span(
        InputSearch(
          t._1.ingredient.name,
          existingIngredients.map(_.name),
          (entered: String) => (name: String) => name.toLowerCase.contains(entered.toLowerCase),
          (name: String) =>
            onUpdate(
              index,
              t._1
                .copy(ingredient = ingredientFromString.optionFromString(name).getOrElse(Ingredient.withName(name))) -> t._2
            ),
          Some((name: String) => existingIngredients.map(_.name).contains(name))
        ),
        InputFromString(
          t._2,
          NumericValidators[Double].positive,
          (returned: InputFromString.ReturnedFromInput[Double]) =>
            onUpdate(
              index,
              returned match {
                case InputFromString.ReturnedFromInput(raw, Some(amount)) => (t._1.copy(amount = amount), raw)
                case InputFromString.ReturnedFromInput(entered, _)        => (t._1, entered)
              }
            )
        )
      )

  def render(): ReactElement = div(
    section(
      h2("Recipes"),
      CustomListForm(
        "Recipes",
        state.recipeListIndices zip props.currentBasket.recipes,
        () => recipeRow,
        (newIndicesAndRecipes: List[(Int, RecipeQuantity)]) => {
          props.changeBasket(props.currentBasket.copy(recipes = newIndicesAndRecipes.map(_._2)))
          setState(_.copy(recipeListIndices = newIndicesAndRecipes.map(_._1)))
        }
      )
    ),
    section(
      h2("More ingredients"),
      CustomListForm(
        "Ingredients",
        state.customIngredientIndices zip (props.currentBasket.extraIngredients zip state.rawAmounts),
        () => customIngredientRow,
        (newIndicesAndIngredients: List[(Int, (IngredientQuantity, String))]) => {
          props.changeBasket(props.currentBasket.copy(extraIngredients = newIndicesAndIngredients.map(_._2._1)))
          setState(
            _.copy(
              customIngredientIndices = newIndicesAndIngredients.map(_._1),
              rawAmounts = newIndicesAndIngredients.map(_._2._2)
            )
          )
        }
      )
    ),
    section(
      button(
        onClick := props.finish,
        "Finish"
      )
    )
  )

}
