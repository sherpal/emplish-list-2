package models.database

import models.emplishlist.db._
import models.emplishlist.{IngredientQuantity, Recipe}
import models.errors
import models.errors.{BackendError, RecipeErrors}
import models.guards.UserAction.SessionRequest
import monix.eval.Task
import utils.database.DBProfile.api._
import utils.database.tables._

trait RecipesDB extends MonixDB {

  private val query = RecipeTable.query

  private def queryJoined =
    query
      .joinLeft(
        IngredientRecipeTable.query
          .join(
            IngredientTable.query
              .joinLeft(
                IngredientsInStoresTable.query
                  .join(StoresTable.query)
                  .on(_.storeId === _.id)
              )
              .on(_.id === _._1.ingredientId)
              .map(elem => (elem._1, elem._2.map(_._2)))
          )
          .on(_.ingredientId === _._1.id)
      )
      .on(_.uniqueId === _._1.recipeId)

  private def queryJoinedResultToRecipe(
      recipes: Vector[(DBRecipe, Option[(DBRecipeIngredient, (DBIngredient, Option[DBStore]))])]
  ): Vector[Recipe] =
    recipes
      .groupBy(_._1)
      .map {
        case (dbRecipe, ingredientsInfo) =>
          Recipe(
            dbRecipe.uniqueId,
            dbRecipe.name,
            ingredientsInfo
              .collect {
                case (_, Some((dbRecipeIngredient, (dbIngredient, Some(dbIngredientInStore))))) =>
                  (dbRecipeIngredient, dbIngredient, dbIngredientInStore)
              }
              .groupBy(_._2)
              .map {
                case (ingredient, info) =>
                  IngredientQuantity(
                    ingredient.toIngredient(info.map(_._3).toList),
                    info.head._1.amount
                  )
              }
              .toList,
            dbRecipe.createdBy,
            dbRecipe.createdOn,
            dbRecipe.lastUpdateBy,
            dbRecipe.lastUpdateOn,
            dbRecipe.description,
            dbRecipe.forHowManyPeople,
            dbRecipe.tags
          )
      }
      .toVector

  def allDBRecipes: Task[Vector[DBRecipe]] = runAsTask(query.result).map(_.toVector)

  def allRecipes: Task[Vector[Recipe]] = runAsTask(queryJoined.result).map(_.toVector).map(queryJoinedResultToRecipe)

  def recipeById(recipeId: Int): Task[Option[Recipe]] =
    runAsTask(
      queryJoined.filter(_._1.uniqueId === recipeId).result
    ).map(_.toVector).map(queryJoinedResultToRecipe).map(_.headOption)

  def recipeExists(recipeId: Int): Task[Boolean] =
    runAsTask(query.filter(_.uniqueId === recipeId).result.headOption).map(_.isDefined)

  def recipeNameExists(recipeName: String): Task[Boolean] =
    runAsTask(query.filter(_.name === recipeName).result.headOption).map(_.isDefined)

  def updateExistingRecipe(recipe: Recipe)(
      implicit sessionRequest: SessionRequest[_]
  ): Task[Either[Map[String, List[BackendError]], Boolean]] = {

    val SessionRequest(_, userName, timestamp, _) = sessionRequest

    val DBRecipeInfo(dbRecipe, dbRecipeIngredients) = recipe.toDBRecipeInfo
    val dbRecipeWithMetadata = dbRecipe.copy(lastUpdateBy = userName, lastUpdateOn = timestamp)

    val updateDBRecipeQuery = query
      .filter(_.uniqueId === dbRecipe.uniqueId)
      .map(r => (r.name, r.lastUpdateBy, r.lastUpdateOn, r.description, r.forHowManyPeople, r.tags))
      .update(dbRecipeWithMetadata.tuple)

    val deleteIngredients = IngredientRecipeTable.query
      .filter(_.recipeId === recipe.uniqueId)
      .delete

    val insertIngredients = IngredientRecipeTable.query ++= dbRecipeIngredients

    (for {
      alreadyExists <- recipeExists(recipe.uniqueId)
      _ <- Task { if (!alreadyExists) throw new errors.RecipeErrors.RecipeDoesNotExists(recipe.uniqueId) }
      _ <- Task {
        if (dbRecipeIngredients.map(_.recipeId).exists(_ != recipe.uniqueId))
          throw new RecipeErrors.IdsDoNotMatch(
            recipe.uniqueId,
            dbRecipeIngredients.map(_.recipeId).find(_ != recipe.uniqueId).get
          )
      }
      _ <- runAsTask(updateDBRecipeQuery)
      _ <- runAsTask(deleteIngredients)
      _ <- runAsTask(insertIngredients)
    } yield Right[Map[String, List[BackendError]], Boolean](true))
      .onErrorRecover {
        case error: RecipeErrors =>
          Left[Map[String, List[BackendError]], Boolean](Map("name" -> List(error.toBackendError)))
      }
  }

  def addNewRecipe(
      recipe: Recipe
  )(implicit sessionRequest: SessionRequest[_]): Task[Either[Map[String, List[BackendError]], Boolean]] = {

    val SessionRequest(_, userName, timestamp, _) = sessionRequest

    val DBRecipeInfo(dbRecipe, dbRecipeIngredients) = recipe.toDBRecipeInfo
    val dbRecipeWithMetadata = dbRecipe.copy(
      createdBy = userName,
      createdOn = timestamp,
      lastUpdateBy = userName,
      lastUpdateOn = timestamp
    )

    val insertRecipeQuery = (query returning query.map(_.uniqueId)) += dbRecipeWithMetadata

    (for {
      alreadyExists <- recipeNameExists(recipe.name)
      _ <- Task { if (alreadyExists) throw new RecipeErrors.RecipeAlreadyExists(recipe.name) }
      newRecipeId <- runAsTask(insertRecipeQuery)
      dbRecipeIngredientsWithId = dbRecipeIngredients.map(_.copy(recipeId = newRecipeId))
      _ <- runAsTask(IngredientRecipeTable.query ++= dbRecipeIngredientsWithId)
    } yield Right[Map[String, List[BackendError]], Boolean](true))
      .onErrorRecover {
        case error: RecipeErrors =>
          Left[Map[String, List[BackendError]], Boolean](Map("name" -> List(error.toBackendError)))
      }

  }

}
