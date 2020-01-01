package frontend.components.ingredients

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, SourceQueueWithComplete}
import akka.stream.{Materializer, QueueOfferResult}
import frontend.components.forms.SimpleForm
import frontend.components.helpers.forms.InputString
import frontend.components.helpers.inputsearch.InputSearch
import frontend.components.helpers.listform.ListForm
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.emplishlist.{Ingredient, IngredientUnit, Store}
import models.errors.BackendError
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import sttp.client.Response

import scala.collection.Map
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@react final class NewIngredientForm extends Component {
  case class Props(
      actorSystem: ActorSystem,
      mat: Materializer,
      ingredients: Vector[Ingredient],
      units: Vector[IngredientUnit],
      stores: Vector[Store],
      lastIngredientAdded: Ingredient => Unit
  )

  implicit def as: ActorSystem = props.actorSystem
  implicit def mat: Materializer = props.mat
  implicit def ec: ExecutionContext = as.dispatcher

  def stores: Vector[Store] = props.stores

  case class State(ingredient: Ingredient, errors: Map[String, List[BackendError]])
  def initialState: State = State(
    Ingredient.empty,
    Ingredient.validator(Vector(), Vector(), Vector())(Ingredient.empty)
  )

  lazy val simpleForm: SimpleForm[State, Ingredient] = SimpleForm[State, Ingredient](
    Ingredient.validator(
      props.ingredients,
      props.units,
      props.stores
    ),
    Sink.foreach(ing => setState(_.copy(ingredient = ing))),
    Sink.foreach(errors => setState(_.copy(errors = errors)))
  )

  lazy val queue: SourceQueueWithComplete[simpleForm.FormDataChanger] = simpleForm.run()

  val changeName: String => Future[QueueOfferResult] = newName => queue.offer(_.copy(name = newName))
  val changeStores: List[Store] => Future[QueueOfferResult] = stores => queue.offer(_.copy(stores = stores))
  val changeUnit: IngredientUnit => Future[QueueOfferResult] = unit => queue.offer(_.copy(unit = unit))

  val changeStoresFromNames: List[String] => Unit =
    storeNames =>
      changeStores(storeNames.map(storeName => stores.find(_.name == storeName).getOrElse(Store(0, storeName))))
  val changeUnitFromName: String => Unit = name => changeUnit(IngredientUnit(name))

  def clearForm(): Unit = {
    setState(_ => initialState)
    queue.offer(_ => initialState.ingredient)
  }

  def submit(): Unit = {
    simpleForm.validator.validate(state.ingredient) match {
      case m if m.isEmpty =>
        println("no error, should send to server")
        boilerplate
          .post(path("ingredients", "add-ingredient"))
          .body(state.ingredient)
          .response(responseAs[Ingredient])
          .send()
          .onComplete {
            case Success(Response(Right(Right(ingredient)), _, _, _, _)) =>
              clearForm()
              props.lastIngredientAdded(ingredient)
            case Success(Response(Left(Right(errors)), _, _, _, _)) =>
              println(errors)
              setState(_.copy(errors = errors))
            case Failure(exception) =>
              throw exception
            case _ =>
              throw new Exception("Failure during de-serialization")
          }
      case m =>
        println("there are errors")
        setState(_.copy(errors = m))
    }

  }

  def render(): ReactElement =
    section(
      h1("New Ingredient"),
      form(onSubmit := { event =>
        event.preventDefault()
        submit()
      })(
        fieldset(
          InputString(state.ingredient.name, "Ingredient name", changeName, Nil) // don't display errors, we don't care
        ),
        fieldset(
          "Unit measure ",
          InputSearch(
            state.ingredient.unit.name,
            props.units.map(_.name).toList,
            x => _.toLowerCase.contains(x.toLowerCase),
            changeUnitFromName,
            Some(props.units.map(_.name).contains)
          )
        ),
        ListForm(
          state.ingredient.stores.map(_.name),
          "Stores",
          changeStoresFromNames,
          Some(props.stores.map(_.name).toList)
        ),
        input(`type` := "submit", value := "Submit", className := Some("invalid").filter(_ => state.errors.nonEmpty))
      )
    )

}
