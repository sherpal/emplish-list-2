package utils.database.tables

import models.emplishlist.db.DBRecipeIngredient
import slick.lifted.Tag
import utils.database.DBProfile.api._

final class IngredientRecipeTable(tag: Tag) extends Table[DBRecipeIngredient](tag, "ingredient_recipe") {

  def ingredientId = column[Int]("ingredient_id", O.PrimaryKey)
  def recipeId = column[Int]("recipe_id")
  def amount = column[Double]("amount")

  def * =
    (ingredientId, recipeId, amount) <> (DBRecipeIngredient.tupled, DBRecipeIngredient.unapply)

}

object IngredientRecipeTable {

  def query: TableQuery[IngredientRecipeTable] = TableQuery[IngredientRecipeTable]

}
