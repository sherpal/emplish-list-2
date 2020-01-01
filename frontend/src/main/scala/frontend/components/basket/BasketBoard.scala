package frontend.components.basket

import frontend.components.helpers.InfoDownloader
import io.circe.generic.auto._
import models.emplishlist.basket.Basket
import models.emplishlist.{Ingredient, Recipe}
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

  override def componentWillMount(): Unit = {

    ingredientsDownloader.downloadInfo[Ingredient](
      "ingredients",
      ingredients => _.copy(maybeExistingIngredients = Some(ingredients.toList))
    )
    recipesDownloader.downloadInfo[Recipe]("recipes", recipes => _.copy(maybeExistingRecipe = Some(recipes.toList)))

  }

  def render(): ReactElement = maybeRecipesAndIngredients match {
    case Some((recipes, ingredients)) =>
      div(
        h1("Create your basket"),
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
