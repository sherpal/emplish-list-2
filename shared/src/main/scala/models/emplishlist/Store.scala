package models.emplishlist

import models.emplishlist.db.DBStore
import syntax.WithUnit

final case class Store(id: Int, name: String) extends Ordered[Store] {
  def toDBStore: DBStore = DBStore(id, name)

  def compare(that: Store): Int = this.name compare that.name
}

object Store {

  implicit def storeUnit: WithUnit[Store] = WithUnit(Store(0, ""))

}
