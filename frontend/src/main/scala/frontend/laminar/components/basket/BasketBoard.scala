package frontend.laminar.components.basket

import com.raquo.laminar.api.L._
import com.raquo.laminar.lifecycle.{NodeDidMount, NodeWasDiscarded, NodeWillUnmount}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import frontend.utils.basket.BasketLoader
import io.circe.generic.auto._
import models.emplishlist.basket.Basket
import models.emplishlist.{Ingredient, Recipe}
import org.scalajs.dom
import org.scalajs.dom.html

object BasketBoard {

  def apply()(implicit actorSystemContainer: ActorSystemContainer): ReactiveHtmlElement[html.Div] = {

    import actorSystemContainer._

    val basket = Var[Basket](Basket.empty)
    val maybeExistingRecipe = new InfoDownloader("recipes")
      .downloadInfo[Vector[Recipe]]("recipes")
      .map(_.map(_.toList))
    val maybeExistingIngredients = new InfoDownloader("ingredients")
      .downloadInfo[Vector[Ingredient]]("ingredients")
      .map(_.map(_.toList))
    val finished = Var[Boolean](false)

    val maybeRecipesAndIngredients: EventStream[(List[Recipe], List[Ingredient])] =
      maybeExistingRecipe
        .combineWith(maybeExistingIngredients)
        .collect {
          case (Some(recipes), Some(ingredients)) => (recipes, ingredients)
        }

    val savingBasket: dom.Event => Unit = (_: dom.Event) => {
      BasketLoader.saveBasket(basket.now)
    }

    def clearBasket(): Unit = {
      BasketLoader.clearBasket()
      basket.update(_ => Basket.empty)
    }

    val element = div(
      child <-- maybeRecipesAndIngredients.map {
        case (recipes, ingredients) =>
          div(
            h1("Create your basket"),
            p(button(onClick --> (_ => clearBasket()), "Clear basket")),
            child <-- finished.signal.map {
              if (_) FinalList(basket.signal.map(_.allIngredients), finished.writer)
              else MakeBasket(basket.writer, recipes, ingredients, finished.writer, BasketLoader.loadBasket)
            }
          )
      }
    )

    //dom.window.addEventListener("beforeunload", savingBasket)

    element.subscribe(_.mountEvents) {
      case NodeDidMount =>
        println("hello")
        dom.window.addEventListener("beforeunload", savingBasket)
        for (savedBasket <- BasketLoader.loadBasket) {
          basket.update(_ => savedBasket)
        }
      case NodeWillUnmount =>
        println("coucou")
        BasketLoader.saveBasket(basket.now)
        dom.window.removeEventListener("beforeunload", savingBasket)
      case NodeWasDiscarded => // do nothing
    }

    element
  }

}
