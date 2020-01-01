package models.emplishlist.db

final case class DBRecipe(
    uniqueId: Int,
    name: String,
    createdBy: String,
    createdOn: Long,
    lastUpdateBy: String,
    lastUpdateOn: Long,
    description: String,
    forHowManyPeople: Int
) {

  def tuple: (String, String, Long, String, Int) = (name, lastUpdateBy, lastUpdateOn, description, forHowManyPeople)

}
