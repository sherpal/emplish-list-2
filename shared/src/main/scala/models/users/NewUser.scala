package models.users

import models.errors.BackendError
import models.validators.StringValidators._
import models.validators.Validator._
import models.validators.{FieldsValidator, Validator}
import syntax.WithUnit

final case class NewUser(name: String, password: String, confirmPassword: String, email: String) {
  def valid: Boolean = password == confirmPassword

  /**
    * Returns a number between 0 and 1 indicating the strentgh of the `password`.
    * 0 means weak, while 1 means strong.
    *
    * We define a number of criteria that we want to impose on a string password, via validators.
    * The strength is the relative number of criteria that pass.
    */
  def passwordStrength: Double = {
    val criteria: List[Validator[String, Any]] = List(
      atLeastLength(8),
      containsDigit,
      containsLowercase,
      containsUppercase
    )

    criteria.count(_(password).isEmpty).toDouble / criteria.length
  }
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

  implicit def newUserWithUnit: WithUnit[NewUser] = WithUnit(NewUser("", "", "", ""))

  def fieldsValidator: FieldsValidator[NewUser, BackendError] =
    FieldsValidator(
      Map(
        "name" -> (nonEmptyString ++ atLeastLength(4) ++ noSpace ++
          doesNotContainAnyOf(List("?", "@", ":", "&", "$", "<", ">", ",", "!", "§", "`", "$")))
          .contraMap[NewUser](_.name),
        "password" -> correctPassword.contraMap[NewUser](_.password),
        "confirmPassword" -> correctPassword.contraMap[NewUser](_.confirmPassword),
        "passwordMatch" -> samePasswords,
        "email" -> emailValidator.contraMap[NewUser](_.email)
      )
    )

}
