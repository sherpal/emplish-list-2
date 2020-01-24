package frontend.components.recipes

import models.RecipeSummary
import router.Link
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class RecipeList extends StatelessComponent {

  case class Props(recipes: Vector[RecipeSummary])

  def render(): ReactElement = div(
    Link(to = "/" + Recipes.newRecipePath.createPath(), text = "New Recipe"),
    table(
      thead(
        tr(
          th("Recipe name"),
          th("Created by")
        )
      ),
      tbody(
        props.recipes.map(r => RecipeRow(r).withKey(r.name))
      )
    )
  )

}
