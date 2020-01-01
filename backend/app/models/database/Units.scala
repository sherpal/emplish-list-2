package models.database

import models.emplishlist.IngredientUnit
import monix.eval.Task
import utils.database.DBProfile.api._
import utils.database.tables.UnitsTable

trait Units extends MonixDB {

  private def query = UnitsTable.query

  def units: Task[Vector[IngredientUnit]] =
    for {
      dbUnits <- runAsTask(query.result)
      asVector = dbUnits.toVector
      asUnits = asVector.map(_.toIngredientUnit)
    } yield asUnits.sorted

}
