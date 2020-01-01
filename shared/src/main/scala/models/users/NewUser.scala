package models.users

import models.errors.BackendError
import models.validators.StringValidators._
import models.validators.Validator._
import models.validators.{FieldsValidator, Validator}

final case class NewUser(name: String, password: String, confirmPassword: String) {
  def valid: Boolean = password == confirmPassword
}

object NewUser {

  def validate(newUser: NewUser): List[BackendError] = validator(newUser)

  def samePasswords: Validator[NewUser, BackendError] =
    simpleValidator[NewUser, BackendError](
      _.valid,
      user => BackendError("validator.passwordConfirmMismatch", user.toString)
    )

  def validator: Validator[NewUser, BackendError] =
    nonEmptyString.contraMap[NewUser](_.name) ++
      atLeastLength(4).contraMap[NewUser](_.name) ++
      correctPassword.contraFlatMap[NewUser](user => List(user.password, user.confirmPassword)) ++
      samePasswords

  def fieldsValidator: FieldsValidator[NewUser, BackendError] =
    FieldsValidator(
      Map(
        "name" -> (nonEmptyString ++ atLeastLength(4)).contraMap[NewUser](_.name),
        "password" -> correctPassword.contraMap[NewUser](_.password),
        "confirmPassword" -> correctPassword.contraMap[NewUser](_.confirmPassword),
        "passwordMatch" -> samePasswords
      )
    )

}
