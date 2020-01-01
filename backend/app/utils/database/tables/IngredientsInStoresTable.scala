package utils.database.tables

import models.emplishlist.db.DBIngredientsInStore
import slick.lifted.Tag
import utils.database.DBProfile.api._

final class IngredientsInStoresTable(tag: Tag) extends Table[DBIngredientsInStore](tag, "ingredients_in_store") {

  def ingredientId = column[Int]("ingredient_id")
  def storeId = column[Int]("store_id")

  def * = (ingredientId, storeId) <> (DBIngredientsInStore.tupled, DBIngredientsInStore.unapply)

}

object IngredientsInStoresTable {

  def query: TableQuery[IngredientsInStoresTable] = TableQuery[IngredientsInStoresTable]

}
