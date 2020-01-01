package models.database

import monix.eval.Task
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.JdbcProfile

trait MonixDB {

  protected def db: JdbcProfile#Backend#Database

  protected def runAsTask[R](a: DBIOAction[R, NoStream, Nothing]): Task[R] = Task.deferFuture(db.run(a))

}
