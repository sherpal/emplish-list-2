package frontend.components.recipes

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, SourceQueueWithComplete}
import akka.stream.{Materializer, QueueOfferResult}
import cats.implicits._
import frontend.components.forms.SimpleForm
import frontend.components.helpers.forms.{InputFromString, InputInt, InputString, InputTextArea}
import frontend.components.helpers.inputsearch.InputSearch
import frontend.components.helpers.listform.CustomListForm
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.emplishlist.{Ingredient, IngredientQuantity, Recipe}
import models.errors.BackendError
import models.validators.NumericValidators
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import sttp.client.Response
import syntax.WithUnit

import scala.collection.Map
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@react final class RecipeForm extends Component {

  case class Props(
      initialRecipe: Recipe,
      ingredients: Vector[Ingredient],
      isNewRecipe: Boolean,
      moveAfterSubmit: () => Unit,
      actorSystem: ActorSystem,
      mat: Materializer
  )

  case class State(
      recipe: Recipe,
      errors: Map[String, List[BackendError]],
      ingredientIds: List[Int],
      enteredAmounts: List[String]
  )

  implicit def as: ActorSystem = props.actorSystem
  implicit def mat: Materializer = props.mat
  implicit def ec: ExecutionContext = as.dispatcher

  implicit def ingredients: List[Ingredient] = props.ingredients.toList.sorted

  override def initialState: State = State(
    props.initialRecipe,
    Recipe.validator.apply(props.initialRecipe),
    props.initialRecipe.ingredients.indices.toList,
    props.initialRecipe.ingredients.map(_.amount.toString)
  )

  lazy val simpleForm: SimpleForm[State, Recipe] = SimpleForm[State, Recipe](
    Recipe.validator,
    Sink.foreach(recipe => setState(_.copy(recipe = recipe))),
    Sink.foreach(errors => setState(_.copy(errors = errors)))
  )(WithUnit(props.initialRecipe))

  lazy val queue: SourceQueueWithComplete[simpleForm.FormDataChanger] = simpleForm.run()

  val changeName: String => Unit = newName => queue.offer(_.copy(name = newName))
  val changeIngredients: List[(IngredientQuantity, String)] => Future[QueueOfferResult] = iqs =>
    queue.offer(_.copy(ingredients = iqs.map(_._1)))
  val changeDescription: String => Unit =
    newDescription => queue.offer(_.copy(description = newDescription))
  val changeNbrOfPeople: Int => Unit =
    nbrOfPeople => queue.offer(_.copy(forHowManyPeople = nbrOfPeople))

  def ingredientQuantitiesListUpdate(newList: List[(Int, (IngredientQuantity, String))]): Unit = {
    changeIngredients(newList.map(_._2))
    setState(_.copy(ingredientIds = newList.map(_._1), enteredAmounts = newList.map(_._2._2)))
  }

  def submit(): Unit = {

    val maybeInvalidAmount = state.recipe.ingredients
      .zip(state.enteredAmounts.map(amount => Try(amount.toDouble).toOption))
      .find {
        case (_, None)                                            => true
        case (IngredientQuantity(_, amount), Some(derivedAmount)) => amount != derivedAmount
      }

    (simpleForm.validator.validate(state.recipe), maybeInvalidAmount) match {
      case (m, None) if m.isEmpty =>
        println("Should send to server")
        boilerplate
          .post(path("recipes", if (props.isNewRecipe) "new-recipe" else "update-recipe"))
          .body(state.recipe)
          .response(responseAs[Recipe])
          .send()
          .onComplete {
            case Success(Response(Right(Right(_)), _, _, _, _)) =>
              props.moveAfterSubmit()
            case Success(Response(Left(Right(errors)), _, _, _, _)) =>
              setState(_.copy(errors = errors))
            case Failure(exception) =>
              throw exception
            case _ =>
              throw new Exception("Failure during de-serialization")
          }
      case (errors, _) =>
        setState(_.copy(errors = errors))
        println("There has been errors")
        for {
          (key, es) <- errors
          e <- es
        } println(key, e.errorKey, e.message)
    }

  }

  def row: CustomListForm.Row[(IngredientQuantity, String)] =
    (index: Int, t: (IngredientQuantity, String), onUpdate: (Int, (IngredientQuantity, String)) => Unit) =>
      span(
        "Name ",
        InputSearch(
          t._1.ingredient.name,
          ingredients.map(_.name),
          (s: String) => (_: String).toLowerCase.contains(s.toLowerCase),
          newName =>
            onUpdate(
              index,
              (
                t._1.copy(
                  ingredient = ingredients.find(_.name == newName).getOrElse(Ingredient.empty.copy(name = newName))
                ),
                t._2
              )
            ),
          Some((newName: String) => ingredients.exists(_.name == newName))
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
        ),
        Some(t._1.ingredient.unit.name).filter(_.nonEmpty)
      )

  def render: ReactElement = section(
    form(onSubmit := { event =>
      event.preventDefault()
      submit()
    })(
      fieldset(
        InputString(state.recipe.name, "Recipe name", changeName, Nil)
      ),
      fieldset(
        InputInt("Number of people", state.recipe.forHowManyPeople, changeNbrOfPeople)
      ),
      CustomListForm(
        "Ingredients",
        state.ingredientIds zip (state.recipe.ingredients zip state.enteredAmounts),
        () => row,
        ingredientQuantitiesListUpdate
      ),
      fieldset(
        InputTextArea("Description", state.recipe.description, changeDescription)
      ),
      input(
        className := (if (simpleForm.validator.isValid(state.recipe)) "valid" else "invalid"),
        `type` := "submit",
        value := "Submit"
      )
    )
  )

}
