package frontend.laminar.components.recipes

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.components.recipes.Recipes
import frontend.laminar.router.{Link, Router}
import models.RecipeSummary
import org.scalajs.dom.html

object RecipeList {

  private def goToRecipe(recipeId: Int): Unit = {
    Router.router.moveTo(Recipes.recipeViewPath(recipeId).createPath())
  }

  private def recipeRow(recipe: RecipeSummary) = tr(
    dataTag(value := recipe.uniqueId.toString),
    td(className := "clickable", onClick.mapTo(recipe.uniqueId) --> (uid => goToRecipe(uid))),
    td(recipe.createdBy)
  )

  def apply(recipes: Vector[RecipeSummary]): ReactiveHtmlElement[html.Div] = div(
    Link(to = Recipes.newRecipePath)(text = "New Recipe"),
    table(
      thead(
        tr(th("Recipe name", "Created by"))
      ),
      tbody(
        recipes.map(recipe => recipeRow(recipe))
      )
    )
  )

}
