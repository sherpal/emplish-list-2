package models.emplishlist.db

import models.emplishlist.IngredientUnit

final case class DBIngredientUnit(name: String) {
  def toIngredientUnit = IngredientUnit(name)
}
