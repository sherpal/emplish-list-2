package controllers

import io.circe.generic.auto._
import javax.inject.Inject
import models.database.{Ingredients, RecipesDB}
import models.emplishlist.Recipe
import models.errors.BackendError
import models.errors.BackendError._
import models.guards.FullAuthGuardFactory
import monix.eval.Task
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.HttpErrorHandler
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import slick.jdbc.JdbcProfile
import utils.ReadsImplicits._
import utils.WriteableImplicits._
import utils.monix.SchedulerProvider

import scala.concurrent.ExecutionContext

final class RecipesController @Inject()(
    assets: Assets,
    errorHandler: HttpErrorHandler,
    config: Configuration,
    authGuard: FullAuthGuardFactory,
    protected val dbConfigProvider: DatabaseConfigProvider,
    cc: ControllerComponents,
    val ec: ExecutionContext
) extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile]
    with SchedulerProvider
    with RecipesDB
    with Ingredients {

  def fetchAllRecipes(): Action[AnyContent] = authGuard().async { implicit request =>
    allRecipes.map(Ok(_)).runToFuture
  }

  def fetchAllDBRecipes(): Action[AnyContent] = authGuard().async { implicit request =>
    allDBRecipes.map(Ok(_)).runToFuture
  }

  def fetchRecipeById(recipeId: Int): Action[AnyContent] = authGuard().async { implicit request =>
    recipeById(recipeId).map(Ok(_)).runToFuture
  }

  def updateRecipe(): Action[Recipe] = authGuard().async(parse.json[Recipe]) { implicit request =>
    val recipe = request.body

    type E = Map[String, List[BackendError]]

    ingredients
      .map(_.toList)
      .flatMap { implicit ingredients =>
        (for {
          errors <- Task.pure(Recipe.validator.apply(recipe))
          mayContinue = if (errors.isEmpty) Right[E, Recipe](recipe) else Left[E, Recipe](errors)
          recipeUpdated <- mayContinue match {
            case Right(recipe) => updateExistingRecipe(recipe)
            case Left(errors)  => Task.pure(Left(errors))
          }
        } yield recipeUpdated).map {
          case Left(errors) => BadRequest(errors)
          case Right(_)     => Ok(recipe)
        }
      }
      .runToFuture
  }

  def addRecipe(): Action[Recipe] = authGuard().async(parse.json[Recipe]) { implicit request =>
    val recipe = request.body

    type E = Map[String, List[BackendError]]

    ingredients.map(_.toList).runToFuture.flatMap { implicit ingredients =>
      (for {
        errors <- Task.pure(Recipe.validator.apply(recipe))
        mayContinue = if (errors.isEmpty) Right[E, Recipe](recipe) else Left[E, Recipe](errors)
        recipeAdded <- mayContinue match {
          case Right(recipe) => addNewRecipe(recipe)
          case Left(errors)  => Task.pure(Left(errors))
        }
      } yield recipeAdded).map {
        case Left(errors) => BadRequest(errors)
        case Right(_)     => Ok(recipe)
      }.runToFuture
    }
  }

}
