package frontend.laminar.components.recipes

import akka.actor.ActorSystem
import com.raquo.laminar.api.L
import com.raquo.laminar.api.L._
import com.raquo.laminar.lifecycle.NodeDidMount
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.components.forms.{FormGroup, ListForm, SimpleForm}
import frontend.laminar.components.helpers.InputSearch
import frontend.laminar.components.helpers.forms.{InputInt, InputString, InputTextArea}
import frontend.laminar.router.Router
import frontend.laminar.utils.ActorSystemContainer
import frontend.utils.Recipes
import frontend.utils.http.DefaultHttp._
import models.emplishlist.{Ingredient, Recipe}
import org.scalajs.dom
import sttp.client.Response
import io.circe.generic.auto._
import models.emplishlist.forms.{UserEnteredIngredientQuantity, UserEnteredRecipe}
import models.errors.BackendError
import models.validators.FieldsValidator
import syntax.WithUnit

import scala.util.{Failure, Success}

final class RecipeForm(
    initialRecipe: Recipe,
    ingredients: Vector[Ingredient],
    isNewRecipe: Boolean,
    recipeValidator: FieldsValidator[Recipe, BackendError]
)(
    implicit val actorSystem: ActorSystem,
    val formDataWithUnit: WithUnit[UserEnteredRecipe]
) extends Component[dom.html.Form]
    with SimpleForm[UserEnteredRecipe] {

  val validator: FieldsValidator[UserEnteredRecipe, BackendError] =
    recipeValidator.maybeContraMap[UserEnteredRecipe, BackendError](
      _.maybeRecipe(ingredients),
      "malformedDouble" -> BackendError("malformedDouble", "One quantity is malformed")
    )

  val nameChanger: WriteBus[String] = createFormDataChanger((name: String) => _.copy(name = name))
  val ingredientsChanger: WriteBus[List[UserEnteredIngredientQuantity]] = createFormDataChanger(
    (iqs: List[UserEnteredIngredientQuantity]) => _.copy(ingredients = iqs)
  )
  val descriptionChanger: WriteBus[String] = createFormDataChanger(
    (description: String) => _.copy(description = description)
  )
  val nbrOfPeopleChanger: WriteBus[Int] = createFormDataChanger((nbr: Int) => _.copy(forHowManyPeople = nbr))

  def submit(): Unit = {

    (validator.validate(formData.now), formData.now.maybeRecipe(ingredients)) match {
      case (m, Some(recipe)) if m.isEmpty =>
        println("Should send to server")
        boilerplate
          .post(path("recipes", if (isNewRecipe) "new-recipe" else "update-recipe"))
          .body(recipe)
          .response(responseAs[Recipe])
          .send()
          .onComplete {
            case Success(Response(Right(Right(_)), _, _, _, _)) =>
              Router.router.moveTo("/" + Recipes.topLevelPath.createPath())
            case Success(Response(Left(Right(errors)), _, _, _, _)) =>
              errorsWriter.onNext(errors)
            case Failure(exception) =>
              throw exception
            case _ =>
              throw new Exception("Failure during de-serialization")
          }
      case (errors, _) =>
        errorsWriter.onNext(errors)
        println("There has been errors")
        for {
          (key, es) <- errors
          e <- es
        } println(key, e.errorKey, e.message)
    }

  }

  val element: ReactiveHtmlElement[dom.html.Form] = {
    val elem = form(
      onSubmit.preventDefault --> (_ => submit()),
      fieldSet(InputString("Recipe name", formData.signal.map(_.name), nameChanger)),
      fieldSet(InputInt("Number of people", formData.signal.map(_.forHowManyPeople), nbrOfPeopleChanger)),
      ListForm[UserEnteredIngredientQuantity](
        "Ingredients",
        formData.signal.map(_.ingredients.zipWithIndex),
        ingredientsChanger.contramapWriter[List[(UserEnteredIngredientQuantity, Int)]](_.map(_._1)),
        RecipeForm.row(_, _, _, ingredients.map(_.name))
      ),
      fieldSet(InputTextArea("Description", formData.signal.map(_.description), descriptionChanger)),
      input(
        className <-- $errors.map(_.isEmpty).map(if (_) "valid" else "invalid"),
        tpe := "submit",
        value := "Submit"
      )
    )

    run()

    setFormData(UserEnteredRecipe.fromRecipe(initialRecipe))

    elem
  }
}

object RecipeForm {

  private final class IngredientQuantityFormGroup(
      val index: Int,
      $iqsWithIndex: Observable[(UserEnteredIngredientQuantity, Int)],
      iqsWithIndexWriter: Observer[(UserEnteredIngredientQuantity, Int)],
      ingredientNames: Vector[String]
  )(implicit unit: WithUnit[UserEnteredIngredientQuantity], owner: Owner)
      extends FormGroup[UserEnteredIngredientQuantity, dom.html.Span] {

    val iq = Var(unit.unit)

    $iqsWithIndex.foreach(_iq => iq.update(_ => _iq._1))

    val element: ReactiveHtmlElement[dom.html.Span] = span(
      "Name ",
      InputSearch[String](
        $iqsWithIndex.map(_._1.ingredientName),
        ingredientNames,
        _ => _ => true,
        identity[String],
        identity[String],
        iqsWithIndexWriter.contramap(s => (iq.now.copy(ingredientName = s), index))
      ),
      " ",
      InputString(
        "Amount ",
        iq.signal.map(_.amount),
        iqsWithIndexWriter.contramap(s => (iq.now.copy(amount = s), index))
      )
    )

    def writer: L.Observer[UserEnteredIngredientQuantity] = iqsWithIndexWriter.contramap(_ -> index)

    def events: L.Observable[UserEnteredIngredientQuantity] = $iqsWithIndex.map(_._1)
  }

  private def row(
      index: Int,
      events: Observable[(UserEnteredIngredientQuantity, Int)],
      writer: Observer[(UserEnteredIngredientQuantity, Int)],
      ingredientNames: Vector[String]
  )(implicit unit: WithUnit[UserEnteredIngredientQuantity], owner: Owner): IngredientQuantityFormGroup =
    new IngredientQuantityFormGroup(index, events, writer, ingredientNames)

  def apply(initialRecipe: Recipe, ingredients: Vector[Ingredient], isNewRecipe: Boolean)(
      implicit actorSystemContainer: ActorSystemContainer
  ): RecipeForm = {
    import actorSystemContainer._
    implicit val is: List[Ingredient] = ingredients.toList
    new RecipeForm(initialRecipe, ingredients, isNewRecipe, Recipe.validator)
  }
}
