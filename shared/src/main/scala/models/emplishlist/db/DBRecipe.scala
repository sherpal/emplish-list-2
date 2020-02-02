package models.emplishlist.db

import models.RecipeSummary

final case class DBRecipe(
    uniqueId: Int,
    name: String,
    createdBy: String,
    createdOn: Long,
    lastUpdateBy: String,
    lastUpdateOn: Long,
    description: String,
    forHowManyPeople: Int,
    tagsAsString: String
) extends RecipeSummary {

  def tags: List[String] = tagsAsString.split(" ").toList.filterNot(_.isEmpty)

  def tuple: (String, String, Long, String, Int, String) =
    (name, lastUpdateBy, lastUpdateOn, description, forHowManyPeople, tagsAsString)

}
