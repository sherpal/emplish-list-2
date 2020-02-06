package models.database

import cats.syntax.applicative._
import models.emplishlist.Ingredient
import models.emplishlist.db.{DBIngredient, DBStore}
import monix.eval.Task
import utils.database.DBProfile.api._
import utils.database.tables.{IngredientTable, IngredientsInStoresTable, StoresTable}

trait Ingredients extends MonixDB with Stores {

  private def query = IngredientTable.query

  def ingredients: Task[Vector[Ingredient]] =
    for {
      dbIngredientsWithStore <- runAsTask(
        query
          .joinLeft(IngredientsInStoresTable.query.join(StoresTable.query).on(_.storeId === _.id))
          .on(_.id === _._1.ingredientId)
          .result
      )
      ingredients = dbIngredientsWithStore
        .groupBy(_._1)
        .mapValues(_.flatMap(_._2).map(_._2).toList)
        .map { case (dbIngredient, stores) => dbIngredient.toIngredient(stores) }
      ingredientsVector = ingredients.toVector
    } yield ingredientsVector.sorted

  def getIngredient(ingredient: Ingredient): Task[Option[DBIngredient]] =
    runAsTask(query.filter(_.name === ingredient.name).take(1).result.headOption)

  def ingredientById(ingredientId: Int): Task[Option[Ingredient]] =
    for {
      maybeIngredient <- runAsTask(query.filter(_.id === ingredientId).take(1).result.headOption)
      dbStores <- maybeIngredient
        .map(_.id)
        .map(
          id =>
            runAsTask(
              IngredientsInStoresTable.query
                .filter(_.ingredientId === id)
                .join(StoresTable.query)
                .on(_.storeId === _.id)
                .map(_._2)
                .result
            )
        )
        .getOrElse(Task(Vector[DBStore]()))
    } yield maybeIngredient.map(_.toIngredient(dbStores.toList))

  private def addIngredientWithStoreInfo(ingredient: Ingredient): Task[Unit] =
    for {
      _ <- runAsTask(query += ingredient.toDBIngredient)
      maybeStoreInfoQuery <- addStoreInfo(ingredient)
      _ <- runAsTask(maybeStoreInfoQuery.get)
    } yield ()

  private def addStoreInfo(ingredient: Ingredient) =
    for {
      maybeIngredient <- getIngredient(ingredient)
      maybeIngredientWithId <- Task.pure(maybeIngredient.map(_.id).map(id => ingredient.copy(id = id)))
      maybeIngredientInStores <- Task.pure(maybeIngredientWithId.map(_.ingredientsInStore))
    } yield maybeIngredientInStores.map(IngredientsInStoresTable.query ++= _)

  def ingredientExists(ingredient: Ingredient): Task[Boolean] =
    for {
      maybeIngredient <- getIngredient(ingredient)
    } yield maybeIngredient.isDefined

  def addIngredientIfNotExists(ingredient: Ingredient): Task[Boolean] =
    for {
      alreadyExists <- ingredientExists(ingredient)
      added <- if (alreadyExists) false.pure[Task] else addIngredientWithStoreInfo(ingredient).map(_ => true)
    } yield added

  def updateIngredient(ingredient: Ingredient): Task[Boolean] =
    for {
      exists <- ingredientExists(ingredient)
      _ <- if (exists)
        runAsTask(
          DBIO
            .seq(
              query
                .filter(_.id === ingredient.id)
                .map(i => (i.unitName, i.tags))
                .update((ingredient.unit.name, ingredient.toDBIngredient.tagsAsString)),
              IngredientsInStoresTable.query.filter(_.ingredientId === ingredient.id).delete,
              IngredientsInStoresTable.query ++= ingredient.ingredientsInStore
            )
            .transactionally
        )
      else Task.pure(())
    } yield exists

  def allIngredientsTag: Task[Vector[String]] =
    runAsTask(query.map(_.tags).distinct.result).map(_.toVector.flatMap(_.split(" ").toVector).distinct)

}
