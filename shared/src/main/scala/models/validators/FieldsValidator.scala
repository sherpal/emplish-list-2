package models.validators

final class FieldsValidator[-T, +E] private (val fields: Map[String, Validator[T, E]]) {

  def validate(t: T): Map[String, List[E]] = fields.mapValues(_(t)).filter(_._2.nonEmpty)

  def apply(t: T): Map[String, List[E]] = validate(t)

  def isValid(t: T): Boolean = validate(t).isEmpty

  def contraMap[U](f: U => T): FieldsValidator[U, E] = FieldsValidator(fields.mapValues(_.contraMap(f)))

  def contraFlatMap[U](f: U => List[T]): FieldsValidator[U, E] = FieldsValidator(fields.mapValues(_.contraFlatMap(f)))

  def toValidator: Validator[T, E] = fields.values.foldLeft[Validator[T, E]](Validator.allowAllValidator)(_ ++ _)

}

object FieldsValidator {

  val allowAllValidator = new FieldsValidator[Any, Nothing](Map())

  def apply[T, E](fields: Map[String, Validator[T, E]]) = new FieldsValidator[T, E](fields)

}
