package frontend.components.recipes

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import frontend.components.helpers.InfoDownloader
import frontend.utils.http.DefaultHttp._
import io.circe.generic.auto._
import models.emplishlist.{Ingredient, Recipe}
import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.div

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

@react final class RecipeEditorContainer extends Component {

  type Props = Unit

  case class State(editedRecipe: Either[String, Option[Recipe]], existingIngredients: Option[Vector[Ingredient]])

  def initialState: State = State(Left("waiting"), None)

  implicit val actorSystem: ActorSystem = ActorSystem("Recipes")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit def ec: ExecutionContext = actorSystem.dispatcher

  override def componentWillUnmount(): Unit = {
    actorSystem.terminate()
  }

  def moveToNew(): Unit =
    dom.document.location.href = Recipes.newRecipePath

  lazy val downloader: InfoDownloader[State] = InfoDownloader("ingredients", setState)

  override def componentDidMount(): Unit = {
    downloader.downloadInfo[Ingredient]("ingredients", ingredients => _.copy(existingIngredients = Some(ingredients)))

    (for {
      lastSegment <- Future.successful(dom.window.location.pathname.split("/").last)
      isNew = lastSegment == "new"
      idAsString <- Future.successful(Try(lastSegment.toInt).toOption)
      maybeExistingRecipe <- idAsString
        .map(
          id =>
            boilerplate
              .response(responseAs[Option[Recipe]])
              .get(pathWithMultipleParams(Map("recipeId" -> List(id.toString)), "recipes", "get"))
              .send()
              .map(_.body)
              .map(Some(_))
        )
        .getOrElse(Future.successful(None))
    } yield (isNew, maybeExistingRecipe)) onComplete {
      case Success((true, _)) =>
        setState(_.copy(editedRecipe = Right(None)))
      case Success((false, Some(Right(Right(Some(recipe)))))) =>
        setState(_.copy(editedRecipe = Right(Some(recipe))))
      case _ =>
        moveToNew()
    }

  }

  def moveAfterSubmit(maybeRecipeId: Option[Int]): Unit =
    dom.document.location.href = maybeRecipeId match {
      case Some(recipeId) => Recipes.recipeViewPath(recipeId)
      case _              => Recipes.topLevelPath
    }

  def render(): ReactElement = state match {
    case State(Right(maybeRecipe), Some(ingredients)) =>
      RecipeEditor(maybeRecipe, ingredients, moveAfterSubmit, actorSystem, mat)
    case _ => div("Loading")
  }

}
