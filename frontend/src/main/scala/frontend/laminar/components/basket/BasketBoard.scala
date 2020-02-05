package frontend.laminar.components.basket

import akka.stream.scaladsl.{Sink, Source}
import com.raquo.laminar.api.L._
import com.raquo.laminar.lifecycle.{NodeDidMount, NodeWasDiscarded, NodeWillUnmount}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.fixlaminar.Fixes
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import frontend.utils.basket.BasketLoader
import io.circe.generic.auto._
import models.emplishlist.basket.Basket
import models.emplishlist.{Ingredient, Recipe}
import org.scalajs.dom
import org.scalajs.dom.html
import streams.sources.ReadFromObservable._

import scala.concurrent.duration._

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

    Source
      .readFromObservable(basket.signal.changes)
      .groupedWithin(200, 1.second)
      .map(_.last)
      .wireTap(_ => println("Saving basket"))
      .to(Sink.foreach(BasketLoader.saveBasket))
      .run()

    for (savedBasket <- BasketLoader.loadBasket) {
      basket.update(_ => savedBasket)
    }

    implicit val element: ReactiveHtmlElement[html.Div] = div(
      Fixes.readMountEvents,
      child <-- maybeRecipesAndIngredients.map {
        case (recipes, ingredients) =>
          div(
            h1("Create your basket"),
            child <-- finished.signal.map {
              if (_) FinalList(basket.signal.map(_.allIngredients), finished.writer)
              else MakeBasket(basket.writer, recipes, ingredients, finished.writer, basket.now)
            }
          )
      }
    )

    finished.signal.changes.filter(identity).mapTo(basket.now).foreach(BasketLoader.saveBasket)

    element.subscribe(_.mountEvents) {
      case NodeDidMount =>
        println("Basket did mount")
        dom.window.addEventListener("beforeunload", savingBasket)
      case NodeWillUnmount =>
        println("Basket will unmount")
        dom.window.removeEventListener("beforeunload", savingBasket)
      case NodeWasDiscarded =>
        println("Basket was discarded")
    }

    element
  }

}
