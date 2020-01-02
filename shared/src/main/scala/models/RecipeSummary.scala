package models

trait RecipeSummary {

  def uniqueId: Int
  def name: String
  def createdBy: String

}

object RecipeSummary {

  def apply(recipeId: Int, name: String, createdBy: String): RecipeSummary = {
    val _recipeId = recipeId
    val _name = name
    val _createdBy = createdBy

    new RecipeSummary {
      def uniqueId: Int = _recipeId
      def name: String = _name
      def createdBy: String = _createdBy
    }
  }

}
