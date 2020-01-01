package models.validators

import models.errors.BackendError
import models.validators.Validator._

object StringValidators {

  type E = BackendError

  private val sv = simpleValidator[String, E] _

  final val nonEmptyString = sv(_.nonEmpty, BackendError("validator.nonEmptyString", _))

  def atLeastLength(n: Int): Validator[String, BackendError] =
    sv(_.length >= n, BackendError("validator.nonLengthString", _))

  final val containsUppercase = sv(_.exists(_.isUpper), BackendError("validator.noUppercase", _))

  final val containsLowercase = sv(_.exists(_.isLower), BackendError("validator.noLowercase", _))

  final val containsDigit = sv(t => """\d""".r.findFirstIn(t).isDefined, BackendError("validator.noDigit", _))

  final val correctPassword = nonEmptyString

  final val emailValidator = sv(_.contains("@"), BackendError("validator.emailContainsAt", _))

}
