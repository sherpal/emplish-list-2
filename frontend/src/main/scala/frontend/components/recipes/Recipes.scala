package frontend.components.recipes

object Recipes {

  final val topLevelPath = "/recipes"

  final val editorPath = topLevelPath + "/editor/"

  final val newRecipePath = editorPath + "new"

  final def editRecipePath(recipeId: Int): String = editorPath + recipeId

  final val viewRecipePath = topLevelPath + "/view/"

  final def recipeViewPath(recipeId: Int): String = viewRecipePath + recipeId

}
