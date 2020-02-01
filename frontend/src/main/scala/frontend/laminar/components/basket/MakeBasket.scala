package frontend.laminar.components.basket

import akka.actor.ActorSystem
import com.raquo.laminar.api.L
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.components.forms.{FormGroup, ListForm, SimpleForm}
import frontend.laminar.components.helpers.InputSearch
import frontend.laminar.components.helpers.forms.{InputInt, InputString}
import models.emplishlist.basket.Basket
import models.emplishlist.forms.UserEnteredIngredientQuantity
import models.emplishlist.{Ingredient, Recipe, RecipeQuantity}
import models.errors.BackendError
import models.validators.FieldsValidator
import org.scalajs.dom
import org.scalajs.dom.html
import syntax.WithUnit

final class MakeBasket private (
    basketWriter: Observer[Basket],
    recipes: List[Recipe],
    ingredients: List[Ingredient],
    finishWriter: Observer[Boolean],
    initialBasket: Basket
)(implicit val formDataWithUnit: WithUnit[Basket], val actorSystem: ActorSystem)
    extends Component[dom.html.Div]
    with SimpleForm[Basket] {

  val validator: FieldsValidator[Basket, BackendError] = FieldsValidator.allowAllValidator

  formData.signal.foreach(basketWriter.onNext)

  val recipeQuantityChanger: WriteBus[List[RecipeQuantity]] =
    createFormDataChanger[List[RecipeQuantity]](ls => _.copy(recipes = ls))

  val extraIngredientsChanger: WriteBus[List[UserEnteredIngredientQuantity]] =
    createFormDataChanger[List[UserEnteredIngredientQuantity]](
      userEnteredIngredients =>
        userEnteredIngredients.map(_.ingredientQuantity(ingredients)) match {
          case ls if ls.forall(_.isDefined) => _.copy(extraIngredients = ls.map(_.get))
          case _                            => identity
        }
    )

  val extraIngredientsBus: EventBus[List[UserEnteredIngredientQuantity]] = new EventBus()
  extraIngredientsBus.events.foreach(extraIngredientsChanger.onNext)

  val element: ReactiveHtmlElement[html.Div] = {
    run()

    createFormDataChanger[Basket](b => _ => b).onNext(initialBasket)
    extraIngredientsBus.writer.onNext(
      initialBasket.extraIngredients
        .map(UserEnteredIngredientQuantity.fromIngredientQuantity)
    )

    div(
      section(
        h2("Recipes"),
        ListForm[RecipeQuantity](
          "",
          formData.signal.map(_.recipes.zipWithIndex),
          recipeQuantityChanger.contramapWriter[List[(RecipeQuantity, Int)]](_.map(_._1)),
          MakeBasket.recipeRow(_, _, _, recipes)
        )
      ),
      section(
        h2("Ingredients"),
        ListForm[UserEnteredIngredientQuantity](
          "",
          extraIngredientsBus.events
            .map(_.zipWithIndex)
            .fold(List[(UserEnteredIngredientQuantity, Int)]())((_, ls) => ls),
          extraIngredientsBus.writer.contramapWriter[List[(UserEnteredIngredientQuantity, Int)]](_.map(_._1)),
          MakeBasket.ingredientRow(_, _, _, ingredients)
        )
      ),
      section(
        button(onClick.mapTo(true) --> finishWriter, "Finish")
      )
    )
  }
}

object MakeBasket {

  private class CustomIngredientsRow(
      index: Int,
      observable: Observable[(UserEnteredIngredientQuantity, Int)],
      observer: Observer[(UserEnteredIngredientQuantity, Int)],
      ingredients: List[Ingredient]
  )(implicit withUnit: WithUnit[UserEnteredIngredientQuantity], owner: Owner)
      extends FormGroup[UserEnteredIngredientQuantity, dom.html.Span] {
    def writer: Observer[UserEnteredIngredientQuantity] = observer.contramap(_ -> index)
    def events: Observable[UserEnteredIngredientQuantity] = observable.map(_._1)

    val iq: Var[UserEnteredIngredientQuantity] = Var(withUnit.unit)
    events.foreach(iq.writer.onNext)

    val element: ReactiveHtmlElement[dom.html.Span] = span(
      "Name ",
      InputSearch[String](
        observable.map(_._1.ingredientName),
        ingredients.map(_.name),
        _ => _ => true,
        identity[String],
        identity[String],
        observer.contramap(s => (iq.now.copy(ingredientName = s), index))
      ),
      " ",
      InputString(
        "Amount ",
        iq.signal.map(_.amount),
        observer.contramap(s => (iq.now.copy(amount = s), index))
      )
    )
  }

  private class RecipeRow(
      index: Int,
      observable: Observable[(RecipeQuantity, Int)],
      observer: Observer[(RecipeQuantity, Int)],
      recipes: List[Recipe]
  )(implicit withUnit: WithUnit[RecipeQuantity], owner: Owner)
      extends FormGroup[RecipeQuantity, dom.html.Span] {
    def writer: L.Observer[RecipeQuantity] = observer.contramap(_ -> index)

    def events: L.Observable[RecipeQuantity] = observable.map(_._1)

    val rq: Var[RecipeQuantity] = Var(withUnit.unit)
    events.foreach(_rq => rq.update(_ => _rq))

    val element: ReactiveHtmlElement[html.Span] = span(
      InputSearch[Recipe](
        events.map(_.recipe),
        recipes,
        _ => _ => true,
        name =>
          recipes
            .find(_.name == name)
            .getOrElse(
              Recipe.withName(name)
            ),
        _.name,
        observer.contramap(recipe => rq.now.copy(recipe = recipe) -> index)
      ),
      " for ",
      InputInt(
        "",
        events.map(_.numberOfPeople),
        observer.contramap(nbrPeople => rq.now.copy(numberOfPeople = nbrPeople) -> index)
      )
    )
  }

  private def ingredientRow(
      index: Int,
      observable: Observable[(UserEnteredIngredientQuantity, Int)],
      observer: Observer[(UserEnteredIngredientQuantity, Int)],
      ingredients: List[Ingredient]
  )(implicit withUnit: WithUnit[UserEnteredIngredientQuantity], owner: Owner): CustomIngredientsRow =
    new CustomIngredientsRow(index, observable, observer, ingredients)

  private def recipeRow(
      index: Int,
      observable: Observable[(RecipeQuantity, Int)],
      observer: Observer[(RecipeQuantity, Int)],
      recipes: List[Recipe]
  )(implicit withUnit: WithUnit[RecipeQuantity], owner: Owner): RecipeRow = new RecipeRow(
    index,
    observable,
    observer,
    recipes
  )

  def apply(
      basketWriter: Observer[Basket],
      recipes: List[Recipe],
      ingredients: List[Ingredient],
      finishWriter: Observer[Boolean],
      initialBasket: Basket
  )(implicit formDataWithUnit: WithUnit[Basket], actorSystem: ActorSystem): ReactiveHtmlElement[html.Div] =
    new MakeBasket(basketWriter, recipes, ingredients, finishWriter, initialBasket)

}
