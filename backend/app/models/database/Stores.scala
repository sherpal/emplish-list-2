package models.database

import models.emplishlist.{Ingredient, Store}
import monix.eval.Task
import utils.database.DBProfile.api._
import utils.database.tables.{IngredientsInStoresTable, StoresTable}

trait Stores extends MonixDB {

  private def storesQuery = StoresTable.query
  private def ingredientsInStoresQuery = IngredientsInStoresTable.query

  def stores: Task[Vector[Store]] = runAsTask(storesQuery.result).map(_.toVector).map(_.map(_.toStore))

  def storesOfIngredient(ingredient: Ingredient): Task[Vector[Store]] =
    runAsTask((for {
      store <- storesQuery
      ingredientInStores <- ingredientsInStoresQuery
      if ingredientInStores.ingredientId === ingredient.id
      if store.id === ingredientInStores.storeId
    } yield store).result).map(_.toVector).map(_.map(_.toStore))

  def addIngredientsInStore(ingredient: Ingredient): Task[Option[Int]] =
    runAsTask(ingredientsInStoresQuery ++= ingredient.ingredientsInStore)

}
