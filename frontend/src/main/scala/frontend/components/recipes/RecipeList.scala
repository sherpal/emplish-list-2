package frontend.components.recipes

import models.emplishlist.Recipe
import models.emplishlist.db.DBRecipe
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.reactrouter.Link
import slinky.web.html._

@react final class RecipeList extends StatelessComponent {

  case class Props(recipes: Vector[Recipe])

  def render(): ReactElement = div(
    span(className := "clickable")(Link(to = Recipes.newRecipePath)("New Recipe")),
    table(
      thead(
        tr(
          th("Recipe name"),
          th("Created by")
        )
      ),
      tbody(
        props.recipes.map(Right[DBRecipe, Recipe]).map(r => RecipeRow(r).withKey(r.right.get.name))
      )
    )
  )

}
