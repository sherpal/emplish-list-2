package frontend.components.helpers.forms

import models.validators.Validator
import slinky.core.facade.ReactElement
import slinky.web.html._
import syntax.FromString

object InputFromString {

  case class ReturnedFromInput[T](raw: String, transformed: Option[T])

  def apply[T](
      v: String,
      validator: Validator[T, Any],
      onChangeCallback: ReturnedFromInput[T] => Unit = (_: ReturnedFromInput[T]) => ()
  )(
      implicit fromString: FromString[T]
  ): ReactElement = input(
    className := (
      for {
        newValue <- fromString.optionFromString(v)
        errors = validator(newValue)
        if errors.isEmpty
      } yield "valid"
    ).getOrElse("invalid"),
    value := v,
    onChange := (
        event =>
          onChangeCallback(
            ReturnedFromInput(
              event.target.value,
              fromString.optionFromString(event.target.value)
            )
          )
      )
  )

}
