package models.validators

import models.errors.BackendError
import models.validators.Validator._

sealed trait NumericValidators[T] {

  val num: Numeric[T]

  type E = BackendError

  private def sv: (T => Boolean, T => E) => Validator[T, E] = simpleValidator[T, E]

  private def error(key: String): T => E = (t: T) => BackendError(key, t.toString)

  def nonZero: Validator[T, E] = sv(_ != num.zero, error("validator.numberZero"))

  def biggerThan(t: T): Validator[T, E] =
    sv(num.compare(_, t) > 0, x => BackendError("validator.numericNotBiggerThan", (x, t).toString))

  def lessThan(t: T): Validator[T, E] =
    sv(num.compare(_, t) < 0, x => BackendError("validator.numericNotLessThan", (x, t).toString))

  def nonNegative: Validator[T, E] =
    sv(num.compare(_, num.zero) > 0, error("validator.numericIsNegative"))

  def positive: Validator[T, E] = nonZero ++ nonNegative

  /** Between `lower` and `upper` included */
  def between(lower: T, upper: T): Validator[T, E] = {
    import num.mkNumericOps
    biggerThan(lower - num.one) ++ lessThan(upper + num.one)
  }

  /** Between `lower` and `upper` excluded */
  def within(lower: T, upper: T): Validator[T, E] = {
    import num.mkNumericOps
    between(lower + num.one, upper - num.one)
  }

}

object NumericValidators {

  def apply[T](implicit n: Numeric[T]): NumericValidators[T] = new NumericValidators[T] {
    val num: Numeric[T] = n
  }

  implicit def invoke[T](implicit num: Numeric[T]): NumericValidators[T] = apply[T]

}
