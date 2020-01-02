package frontend.utils.basket

import models.emplishlist.basket.Basket
import org.scalajs.dom
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import models.emplishlist.{IngredientQuantity, RecipeQuantity}
import syntax.WithUnit

object BasketLoader {

  private final val basketStorageKey = "basket"

  def loadBasket: Option[Basket] =
    for {
      basketJson <- Option(dom.window.sessionStorage.getItem(basketStorageKey))
      basket <- decode[Basket](basketJson).toOption
    } yield basket

  def saveBasket(basket: Basket): Unit = dom.window.sessionStorage.setItem(basketStorageKey, basket.asJson.noSpaces)

  def addIngredient(ingredientQuantity: IngredientQuantity)(implicit basketUnit: WithUnit[Basket]): Unit = {
    val currentBasket = loadBasket.getOrElse(basketUnit.unit)

    val newIngredients =
      if (currentBasket.extraIngredients.map(_.ingredient.name).contains(ingredientQuantity.ingredient.name)) {
        currentBasket.extraIngredients.map(
          iq => if (iq.ingredient.name == ingredientQuantity.ingredient.name) iq + ingredientQuantity.amount else iq
        )
      } else currentBasket.extraIngredients :+ ingredientQuantity

    saveBasket(
      currentBasket.copy(
        extraIngredients = newIngredients
      )
    )
  }

  def addRecipe(recipeQuantity: RecipeQuantity)(implicit basketUnit: WithUnit[Basket]): Unit = {
    val currentBasket = loadBasket.getOrElse(basketUnit.unit)
    saveBasket(currentBasket.copy(recipes = currentBasket.recipes :+ recipeQuantity))
  }

  def clearBasket(): Unit = {
    dom.window.sessionStorage.removeItem(basketStorageKey)
  }

}
