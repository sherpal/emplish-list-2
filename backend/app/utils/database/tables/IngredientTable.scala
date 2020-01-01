package utils.database.tables

import models.emplishlist.db.DBIngredient
import slick.lifted.Tag
import utils.database.DBProfile.api._

final class IngredientTable(tag: Tag) extends Table[DBIngredient](tag, "ingredient") {

  def id = column[Int]("unique_id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.Unique)
  def unitName = column[String]("unit_name")

  def * = (id, name, unitName) <> (DBIngredient.tupled, DBIngredient.unapply)

}

object IngredientTable {

  def query: TableQuery[IngredientTable] = TableQuery[IngredientTable]

}
