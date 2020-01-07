package models

final case class OfficialEmailPassword(password: String, fakeKey: String)

object OfficialEmailPassword {

  /**
    * This is used to somehow mitigate the fact that a robot could go the that particular endpoint.
    */
  final val fakeKey = "thisIsAFakeKey"

  def password(pw: String): OfficialEmailPassword = OfficialEmailPassword(pw, fakeKey)

}
