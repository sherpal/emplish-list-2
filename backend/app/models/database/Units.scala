package models.database

import models.emplishlist.IngredientUnit
import monix.eval.Task
import slick.jdbc.JdbcBackend
import utils.database.DBProfile.api._
import utils.database.tables.UnitsTable
import zio.{Has, ZIO, ZLayer}

trait Units extends MonixDB {

  private def query = UnitsTable.query

  def units: Task[Vector[IngredientUnit]] =
    for {
      dbUnits <- runAsTask(query.result)
      asVector = dbUnits.toVector
      asUnits = asVector.map(_.toIngredientUnit)
    } yield asUnits.sorted

}

object Units {

  private def query = UnitsTable.query

  trait Service {
    def units: zio.Task[Vector[IngredientUnit]]
  }

  def liveUnits: ZLayer[DBProvider, Nothing, Has[Units.Service]] = ZLayer.fromFunction(
    dbProvider =>
      new Service {
        implicit val db: JdbcBackend#DatabaseDef = dbProvider.get
        def units: zio.Task[Vector[IngredientUnit]] =
          for {
            dbUnits <- runAsTask(query.result)
            asVector = dbUnits.toVector
            asUnits = asVector.map(_.toIngredientUnit)
          } yield asUnits.sorted
      }
  )

  def units: ZIO[Has[Units.Service], Throwable, Vector[IngredientUnit]] = ZIO.accessM(_.get.units)

}
