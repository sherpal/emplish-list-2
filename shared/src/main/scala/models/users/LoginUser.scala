package models.users

import models.errors.BackendError
import models.validators.FieldsValidator
import models.validators.StringValidators._
import syntax.WithUnit

final case class LoginUser(name: String, password: String)

object LoginUser {

  implicit def loginUserWithUnit: WithUnit[LoginUser] = WithUnit(LoginUser("", ""))

  def validator: FieldsValidator[LoginUser, BackendError] =
    FieldsValidator(
      Map(
        "name" -> nonEmptyString.contraMap[LoginUser](_.name),
        "password" -> nonEmptyString.contraMap[LoginUser](_.password)
      )
    )

}
