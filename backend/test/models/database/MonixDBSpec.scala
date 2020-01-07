package models.database

import monix.eval.Task
import org.scalatest.{FlatSpec, Matchers}
import utils.monix.SchedulerProvider

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

final class MonixDBSpec extends FlatSpec with Matchers with MonixDB with SchedulerProvider {

  protected def db: Null = ???

  implicit lazy val ec: ExecutionContext = global

  "foldLeftTasks" should "do the same as foldLeft for pure tasks" in {

    val list = List(1, 2, 3, 4, 5)

    def fold(x: String, u: Int) = x + ";" + u

    val unit = "start"

    list.foldLeft(unit)(fold) should be(
      Await.result(
        foldLeftTasks(list.map(Task.pure), (x: String, u: Task[Int]) => u.map(fold(x, _)), unit).runToFuture,
        3.seconds
      )
    )

  }

}
