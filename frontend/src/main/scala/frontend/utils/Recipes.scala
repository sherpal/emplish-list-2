package frontend.utils

import urldsl.errors.DummyError
import urldsl.language.PathSegment
import urldsl.language.PathSegment.dummyErrorImpl._

object Recipes {

  final val topLevelPath = root / "recipes"

  final val editorPath = topLevelPath / "editor"

  final val newRecipePath = editorPath / "new"

  final def editRecipePath(recipeId: Int): PathSegment[Unit, DummyError] = editorPath / recipeId

  final val viewRecipePath = topLevelPath / "view"

  final def recipeViewPath(recipeId: Int): PathSegment[Unit, DummyError] = viewRecipePath / recipeId

}
