package frontend.components.helpers.inputsearch

import org.scalajs.dom.html
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.{ReactElement, ReactRef}
import slinky.web.html._

import scala.scalajs.js

@react final class SearchList extends StatelessComponent {

  case class Props(
      filteredElements: List[String],
      onSelect: String => Unit,
      reactRef: ReactRef[html.UList],
      anchor: (Double, Double), // top left position to attach the search list to
      width: Double,
      isVisible: Boolean
  )

  private def filteredElements = props.filteredElements

  private def isVisible = props.isVisible && filteredElements.nonEmpty
  private def display = if (isVisible) "block" else "none"

  private def left = props.anchor._1 + "px"
  private def top = props.anchor._2 + "px"
  private def width = props.width + "px"

  override def render(): ReactElement =
    ul(
      ref := props.reactRef,
      className := "search-list-ul",
      style := js.Dynamic.literal(
        display = display,
        position = "absolute",
        zIndex = "2",
        top = top,
        left = left,
        width = width,
        backgroundColor = "white"
      )
    )(
      filteredElements.take(5).map(x => li(key := x, onMouseDown := (() => props.onSelect(x)))(x))
    )

}
