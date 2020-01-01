package frontend.components.recipes

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import models.emplishlist.{Ingredient, Recipe}
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class RecipeEditor extends Component {

  type State = Unit

  def initialState: State = ()

  /**
    *
    * @param maybeRecipe None if a new recipe, or the [[models.emplishlist.Recipe]] if we need to update it.
    */
  case class Props(
      maybeRecipe: Option[Recipe],
      ingredients: Vector[Ingredient],
      moveAfterSubmit: Option[Int] => Unit,
      actorSystem: ActorSystem,
      mat: ActorMaterializer
  )

  def isNewRecipe: Boolean = props.maybeRecipe.isEmpty

  def header: ReactElement = props.maybeRecipe match {
    case Some(recipe) => h1(s"Edit: ${recipe.name} (by ${recipe.createdBy})")
    case None         => h1("New recipe")
  }

  def render(): ReactElement = div(
    header,
    RecipeForm(
      props.maybeRecipe.getOrElse(Recipe.empty),
      props.ingredients,
      isNewRecipe,
      () => props.moveAfterSubmit(props.maybeRecipe.map(_.uniqueId)),
      props.actorSystem,
      props.mat
    )
  )

}
