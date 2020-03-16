package models

import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.{JdbcBackend, JdbcProfile}
import zio.ZLayer.NoDeps
import zio.{Has, Task, ZIO, ZLayer}

package object database {

  type DBProvider = Has[JdbcProfile#Backend#Database]

  def runAsTask[R](a: DBIOAction[R, NoStream, Nothing])(implicit db: JdbcProfile#Backend#Database): Task[R] =
    ZIO.fromFuture { implicit ec =>
      db.run(a)
    }

  def dbProvider(db: JdbcProfile#Backend#Database): NoDeps[Nothing, Has[JdbcBackend#DatabaseDef]] = ZLayer.succeed(db)

}
