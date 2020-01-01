package utils.database.tables

import models.emplishlist.db.DBStore
import slick.lifted.Tag
import utils.database.DBProfile.api._

final class StoresTable(tag: Tag) extends Table[DBStore](tag, "store") {

  def id = column[Int]("unique_id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.Unique)

  def * = (id, name) <> (DBStore.tupled, DBStore.unapply)

}

object StoresTable {

  def query: TableQuery[StoresTable] = TableQuery[StoresTable]

}
