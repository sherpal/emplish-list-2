package models.database

import monix.eval.Task
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.JdbcProfile
import syntax.WithUnit

trait MonixDB {

  protected def db: JdbcProfile#Backend#Database

  protected def runAsTask[R](a: DBIOAction[R, NoStream, Nothing]): Task[R] = Task.deferFuture(db.run(a))

  def foldLeftTasks[T, U](tasks: List[Task[T]], fold: (U, Task[T]) => Task[U], unit: U): Task[U] = {

    @scala.annotation.tailrec
    def accumulator(remainingTasks: List[Task[T]], acc: Task[U]): Task[U] = {
      if (remainingTasks.isEmpty) acc
      else accumulator(remainingTasks.tail, acc.flatMap(fold(_, remainingTasks.head)))
    }

    accumulator(tasks, Task.pure(unit))

  }

  def foldLeftTasks[T, U](tasks: List[Task[T]], fold: (U, Task[T]) => Task[U])(implicit unit: WithUnit[U]): Task[U] =
    foldLeftTasks(tasks, fold, unit.unit)

  def exists[T](tasks: List[Task[T]], predicate: T => Task[Boolean]): Task[Boolean] =
    foldLeftTasks[T, Boolean](
      tasks,
      (left: Boolean, right: Task[T]) => if (left) Task.pure(left) else right.flatMap(predicate),
      false
    )

}
