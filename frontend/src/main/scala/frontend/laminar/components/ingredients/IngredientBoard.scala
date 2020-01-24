package frontend.laminar.components.ingredients

import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.laminar.api.L._
import frontend.laminar.components.helpers.SimpleList
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import models.emplishlist.{Ingredient, IngredientUnit, Store}
import org.scalajs.dom.html
import io.circe.generic.auto._

object IngredientBoard {

  private def unitList(units: Vector[IngredientUnit]) =
    SimpleList("Units", units, (_: IngredientUnit).name, section)

  private def storesList(stores: Vector[Store]) =
    SimpleList("Stores", stores, (_: Store).name, section)

  def apply()(implicit actorSystemContainer: ActorSystemContainer): ReactiveHtmlElement[html.Div] = {
    import actorSystemContainer._

    val downloader = new InfoDownloader("ingredients")

    div(
      child <-- downloader
        .downloadInfo[Vector[Ingredient]]("ingredients")
        .collect { case Some(ingredients) => ingredients }
        .map(IngredientList.apply),
      child <-- downloader
        .downloadInfo[Vector[IngredientUnit]]("units")
        .collect { case Some(units) => units }
        .map(unitList),
      child <-- downloader
        .downloadInfo[Vector[Store]]("stores")
        .collect { case Some(stores) => stores }
        .map(storesList)
    )
  }

}
