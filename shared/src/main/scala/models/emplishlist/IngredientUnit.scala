package models.emplishlist

import models.emplishlist.db.DBIngredientUnit
import syntax.WithUnit

final case class IngredientUnit(name: String) extends Ordered[IngredientUnit] {
  def toDBIngredientUnit: DBIngredientUnit = DBIngredientUnit(name)

  def compare(that: IngredientUnit): Int = this.name compare that.name
}

object IngredientUnit {

  implicit def iuUnit: WithUnit[IngredientUnit] = WithUnit(IngredientUnit(""))

}
