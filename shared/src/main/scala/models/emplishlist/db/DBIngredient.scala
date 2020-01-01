package models.emplishlist.db

import models.emplishlist.{Ingredient, IngredientUnit}

final case class DBIngredient(id: Int, name: String, unitName: String) {
  def toIngredient(stores: List[DBStore]): Ingredient =
    Ingredient(id, name, IngredientUnit(unitName), stores.map(_.toStore))
}
