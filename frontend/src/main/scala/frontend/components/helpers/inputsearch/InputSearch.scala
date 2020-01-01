package frontend.components.helpers.inputsearch

import org.scalajs.dom.html
import org.scalajs.dom.html.{Input, UList}
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.{React, ReactElement, ReactRef}
import slinky.web.html._

import scala.scalajs.js.timers.setTimeout

@react final class InputSearch extends Component {

  case class Props(
      value: String,
      elements: List[String],
      filter: String => String => Boolean,
      givenOnChange: String => Unit,
      isValid: Option[String => Boolean] = None
  )
  case class State(showSearchList: Boolean)

  override def initialState: State = State(showSearchList = false)

  def elements: List[String] = props.elements
  def filter: String => String => Boolean = props.filter

  def filteredElements: List[String] = elements.filter(filter(props.value))

  val inputRef: ReactRef[Input] = React.createRef[html.Input]
  def in: Option[Input] = Option(inputRef.current)

  def bottomLeft: (Double, Double) = in.map(_.getClientRects()(0)).map(cr => (cr.left, cr.bottom)).getOrElse((0.0, 0.0))

  def inputWidth: Double = in.map(_.getClientRects()(0)).map(cr => cr.right - cr.left).getOrElse(0.0)

  val suggestionsRef: ReactRef[UList] = React.createRef[html.UList]
  def suggestionsUL: UList = suggestionsRef.current

  def searchListVisible: Boolean = state.showSearchList && props.value.nonEmpty

  def render(): ReactElement = span(
    input(
      className := props.isValid.map(v => if (v(props.value)) "valid" else "invalid"),
      `type` := "text",
      ref := inputRef,
      onChange := { event =>
        val value = event.target.value
        props.givenOnChange(value)
        setState(_.copy(showSearchList = true))
      },
      onBlur := { () =>
        setTimeout(0) {
          setState(_.copy(showSearchList = false))
        }
      },
      value := props.value
    ),
    SearchList(
      filteredElements,
      x => {
        props.givenOnChange(x)
        setState(_.copy(showSearchList = false))
      },
      suggestionsRef,
      bottomLeft,
      inputWidth,
      searchListVisible
    )
  )

}
