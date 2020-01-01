package utils.database.tables

import models.DBUser
import org.mindrot.jbcrypt.BCrypt
import slick.lifted.{TableQuery, Tag}
import utils.database.DBProfile.api._

final class UsersTable(tag: Tag) extends Table[DBUser](tag, "users") {

  def id = column[String]("id", O.PrimaryKey)
  def name = column[String]("name", O.Unique)
  def password = column[String]("password")

  def * = (id, name, password) <> (DBUser.tupled, DBUser.unapply)

}

object UsersTable {

  type UserName = String
  type Password = String

  def query: TableQuery[UsersTable] = TableQuery[UsersTable]

  def hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(13))

}
