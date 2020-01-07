package frontend.components.helpers.miscellaneous

import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.scalajs.js

object StatusBar {

  case class StatusBarOptions(
      minValue: Double = 0,
      maxValue: Double = 100,
      width: String = "200px",
      height: String = "20px",
      colour: Double => String = (_: Double) => "#00cc66"
  )

  def defaultOptions: StatusBarOptions = StatusBarOptions()

  /**
    * Renders a status bar in a span filled with the given value.
    *
    * The `options` parameter allows to fine tune the display.
    *
    * The `colour` function allows to render a different colour depending on the value.
    */
  def apply(value: Double, options: StatusBarOptions = defaultOptions): ReactElement =
    span(
      style := js.Dynamic.literal(
        width = options.width,
        height = options.height,
        border = "1px solid black",
        display = "inline-block"
      )
    )(
      span(
        style := js.Dynamic.literal(
          width = s"${(value - options.minValue) / (options.maxValue - options.minValue) * 100}%",
          height = "100%",
          backgroundColor = options.colour(value),
          display = "inline-block"
        )
      )
    )

}
