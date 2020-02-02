package models

trait RecipeSummary {

  def uniqueId: Int
  def name: String
  def createdBy: String
  def tags: List[String]

}

object RecipeSummary {

  def apply(recipeId: Int, name: String, createdBy: String, tags: List[String]): RecipeSummary = {
    val _recipeId = recipeId
    val _name = name
    val _createdBy = createdBy
    val _tags = tags

    new RecipeSummary {
      def uniqueId: Int = _recipeId
      def name: String = _name
      def createdBy: String = _createdBy
      def tags: List[String] = _tags
    }
  }

}
