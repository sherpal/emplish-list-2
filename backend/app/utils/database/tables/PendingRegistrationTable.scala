package utils.database.tables

import models.DBPendingRegistration
import slick.lifted.{TableQuery, Tag}
import utils.database.DBProfile.api._

final class PendingRegistrationTable(tag: Tag) extends Table[DBPendingRegistration](tag, "pending_registration") {

  def name = column[String]("name", O.Unique, O.PrimaryKey)
  def password = column[String]("password")
  def email = column[String]("email")
  def randomKey = column[String]("random_key")

  def * = (name, email, password, randomKey) <> (DBPendingRegistration.tupled, DBPendingRegistration.unapply)

}

object PendingRegistrationTable {

  def query: TableQuery[PendingRegistrationTable] = TableQuery[PendingRegistrationTable]

}
