package frontend.laminar.components.recipes

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.forms.Tags
import frontend.laminar.components.helpers.InputTags
import frontend.laminar.router.{Link, Router}
import frontend.utils.Recipes
import models.RecipeSummary
import org.scalajs.dom.html

object RecipeList {

  private def goToRecipe(recipeId: Int): Unit = {
    Router.router.moveTo(Recipes.recipeViewPath(recipeId).createPath())
  }

  private def recipeRow(recipe: RecipeSummary) = tr(
    td(
      className := "clickable",
      onClick.mapTo(recipe.uniqueId) --> (uid => goToRecipe(uid)),
      dataTag(value := recipe.uniqueId.toString),
      recipe.name
    ),
    td(recipe.createdBy)
  )

  def apply(recipes: Vector[RecipeSummary]): ReactiveHtmlElement[html.Div] = {

    val tagsFilter = new EventBus[List[String]]()

    val $tags = tagsFilter.events.fold(List[String]())((_, tags) => tags)
    val $rows = $tags.map(tags => recipes.filter(recipe => tags.forall(recipe.tags.contains)))
      .map(_.map(recipeRow))

    div(
      Link(to = Recipes.newRecipePath)(text = "New Recipe"),
      section(
        Tags.tagsFilterExplanation("recipe"),
        p(
          InputTags($tags, tagsFilter.writer)
        )
      ),
      table(
        thead(
          tr(th("Recipe name"), th("Created by"))
        ),
        tbody(
          children <-- $rows
        )
      )
    )
  }

}
