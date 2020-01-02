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
    forHowManyPeople: Int
) extends RecipeSummary {

  def tuple: (String, String, Long, String, Int) = (name, lastUpdateBy, lastUpdateOn, description, forHowManyPeople)

}
