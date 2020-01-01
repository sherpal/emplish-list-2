package models.emplishlist.db

import models.emplishlist.Store

final case class DBStore(id: Int, name: String) {
  def toStore: Store = Store(id, name)
}
