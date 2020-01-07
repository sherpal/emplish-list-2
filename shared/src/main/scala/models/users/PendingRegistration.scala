package models.users

final case class PendingRegistration(name: String, email: String, hashedPassword: String, randomKey: String)
