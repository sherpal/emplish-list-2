package models

import models.users.{NewUser, PendingRegistration}
import utils.database.tables.UsersTable

final case class DBPendingRegistration(name: String, email: String, hashedPassword: String, randomKey: String) {
  def pendingRegistration: PendingRegistration = PendingRegistration(name, email, hashedPassword, randomKey)
}

object DBPendingRegistration {

  def fromNewUser(newUser: NewUser, key: String): DBPendingRegistration = DBPendingRegistration(
    newUser.name,
    newUser.email,
    UsersTable.hashPassword(newUser.password),
    key
  )

  def fromPendingRegistration(pendingRegistration: PendingRegistration): DBPendingRegistration = DBPendingRegistration(
    pendingRegistration.name,
    pendingRegistration.email,
    pendingRegistration.hashedPassword,
    pendingRegistration.randomKey
  )

  final val tupled = (apply _).tupled

}
