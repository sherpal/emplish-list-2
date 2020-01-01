package utils.database.tables

import models.emplishlist.db.DBIngredientUnit
import slick.lifted.Tag
import utils.database.DBProfile.api._

final class UnitsTable(tag: Tag) extends Table[DBIngredientUnit](tag, "unit") {

  def name = column[String]("name", O.PrimaryKey, O.Unique)

  def * = name <> ((n: String) => DBIngredientUnit(n), DBIngredientUnit.unapply)

}

object UnitsTable {

  def query: TableQuery[UnitsTable] = TableQuery[UnitsTable]

}
