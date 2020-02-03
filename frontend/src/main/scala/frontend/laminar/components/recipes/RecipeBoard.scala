package frontend.laminar.components.recipes

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import io.circe.generic.auto._
import models.emplishlist.Recipe
import org.scalajs.dom
import org.scalajs.dom.html.Div

final class RecipeBoard private ()(implicit actorSystemContainer: ActorSystemContainer)
    extends Component[dom.html.Div] {

  import actorSystemContainer._

  val downloader = new InfoDownloader("recipes")

  val $recipes: EventStream[ReactiveHtmlElement[Div]] = downloader
    .downloadInfo[Vector[Recipe]]("recipes")
    .collect { case Some(recipes) => recipes }
    .map(RecipeList.apply)

  val element: ReactiveHtmlElement[Div] = div(
    h1("Recipes"),
    child <-- $recipes
  )

}

object RecipeBoard {

  def apply()(implicit actorSystemContainer: ActorSystemContainer): RecipeBoard = new RecipeBoard()

}
