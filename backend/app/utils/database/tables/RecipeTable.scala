package utils.database.tables

import models.emplishlist.db.DBRecipe
import slick.lifted.Tag
import utils.database.DBProfile.api._

final class RecipeTable(tag: Tag) extends Table[DBRecipe](tag, "recipe") {

  def uniqueId = column[Int]("unique_id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.Unique)
  def createdBy = column[String]("created_by")
  def createdOn = column[Long]("created_on")
  def lastUpdateBy = column[String]("last_update_by")
  def lastUpdateOn = column[Long]("last_update_on")
  def description = column[String]("description")
  def forHowManyPeople = column[Int]("for_how_many_people")

  def * =
    (uniqueId, name, createdBy, createdOn, lastUpdateBy, lastUpdateOn, description, forHowManyPeople) <>
      (DBRecipe.tupled, DBRecipe.unapply)

}

object RecipeTable {

  def query: TableQuery[RecipeTable] = TableQuery[RecipeTable]

}
