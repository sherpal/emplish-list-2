package frontend.components.basket

import frontend.components.helpers.InfoDownloader
import frontend.utils.basket.BasketLoader
import io.circe.generic.auto._
import models.emplishlist.basket.Basket
import models.emplishlist.{Ingredient, Recipe}
import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.concurrent.ExecutionContext.Implicits.global

@react final class BasketBoard extends Component {
  type Props = Unit

  case class State(
      basket: Basket,
      maybeExistingRecipe: Option[List[Recipe]] = None,
      maybeExistingIngredients: Option[List[Ingredient]] = None,
      finished: Boolean = false
  )

  def maybeRecipesAndIngredients: Option[(List[Recipe], List[Ingredient])] =
    for {
      recipes <- state.maybeExistingRecipe
      ingredients <- state.maybeExistingIngredients
    } yield (recipes, ingredients)

  def finish(isFinished: Boolean): () => Unit = () => {
    setState(_.copy(finished = isFinished))
  }

  def initialState: State = State(Basket.empty)

  lazy val ingredientsDownloader: InfoDownloader[State] = InfoDownloader("ingredients", setState)
  lazy val recipesDownloader: InfoDownloader[State] = InfoDownloader("recipes", setState)

  val savingBasket: dom.Event => Unit = (_: dom.Event) => {
    BasketLoader.saveBasket(state.basket)
    dom.window.alert("coucou")
  }

  override def componentWillUnmount(): Unit = {
    BasketLoader.saveBasket(state.basket)
    dom.window.removeEventListener("beforeunload", savingBasket)
  }

  override def componentWillMount(): Unit = {

    dom.window.addEventListener("beforeunload", savingBasket)

    for (basket <- BasketLoader.loadBasket) {
      setState(_.copy(basket = basket))
    }

    ingredientsDownloader.downloadInfo[Ingredient](
      "ingredients",
      ingredients => _.copy(maybeExistingIngredients = Some(ingredients.toList))
    )
    recipesDownloader.downloadInfo[Recipe]("recipes", recipes => _.copy(maybeExistingRecipe = Some(recipes.toList)))

  }

  def clearBasket(): Unit = {
    BasketLoader.clearBasket()
    setState(_.copy(basket = Basket.empty))
  }

  def render(): ReactElement = maybeRecipesAndIngredients match {
    case Some((recipes, ingredients)) =>
      div(
        h1("Create your basket"),
        p(button(onClick := clearBasket _, "Clear basket")),
        if (state.finished) FinalList(state.basket.allIngredients, finish(isFinished = false))
        else {
          MakeBasket(
            state.basket,
            basket => setState(_.copy(basket = basket)),
            recipes,
            ingredients,
            finish(isFinished = true)
          )
        }
      )
    case None => div("loading...")
  }
}
