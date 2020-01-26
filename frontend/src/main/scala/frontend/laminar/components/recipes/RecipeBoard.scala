package frontend.laminar.components.recipes

import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.laminar.api.L._
import frontend.laminar.components.Component
import frontend.laminar.utils.{ActorSystemContainer, InfoDownloader}
import org.scalajs.dom
import org.scalajs.dom.html.Div
import io.circe.generic.auto._
import models.emplishlist.Recipe

final class RecipeBoard private ()(implicit actorSystemContainer: ActorSystemContainer)
    extends Component[dom.html.Div] {

  import actorSystemContainer._

  val downloader = new InfoDownloader("recipes")

  val element: ReactiveHtmlElement[Div] = div(
    h1("Recipes"),
    child <-- downloader
      .downloadInfo[Vector[Recipe]]("recipes")
      .collect { case Some(recipes) => recipes }
      .map(RecipeList.apply)
  )

}

object RecipeBoard {

  def apply()(implicit actorSystemContainer: ActorSystemContainer): RecipeBoard = new RecipeBoard()

}
