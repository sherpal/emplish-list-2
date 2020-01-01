package models.errors

abstract class RecipeErrors(key: String, message: String) extends Exception(message) {
  def toBackendError: BackendError = BackendError(key, message)
}

object RecipeErrors {

  final class RecipeAlreadyExists(recipeName: String) extends RecipeErrors("recipeAlreadyExists", recipeName)

  final class RecipeDoesNotExists(recipeId: Int) extends RecipeErrors("recipeDoesNotExist", recipeId.toString)

  final class IdsDoNotMatch(recipeId: Int, wrongId: Int)
      extends RecipeErrors("idsDoNotMatch", (recipeId, wrongId).toString)

}
