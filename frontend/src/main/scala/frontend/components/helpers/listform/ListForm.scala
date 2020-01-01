package frontend.components.helpers.listform

import frontend.components.helpers.listform.RowMessage.ValueChanged
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class ListForm extends Component {

  case class Props(
      values: List[String],
      title: String,
      onListChange: List[String] => Unit,
      maybePossibleChoices: Option[List[String]]
  )

  case class State(lastId: Int, ids: List[Int])

  def idsAndValues: List[(Int, String)] = state.ids zip props.values

  override def initialState: State = State(0, List(0))

  def setStateWarnParent(changeState: State => State, newValues: List[String]): Unit = {
    setState(changeState)
    props.onListChange(newValues)
  }

  val add: () => Unit = () =>
    setStateWarnParent(s => s.copy(lastId = s.lastId + 1, ids = s.ids :+ (s.lastId + 1)), props.values :+ "")

  val delete: Int => Unit = (idx: Int) =>
    setStateWarnParent(
      s =>
        if (s.ids.nonEmpty && s.ids.tail.nonEmpty)
          s.copy(ids = s.ids.filterNot(_ == idx))
        else s,
      idsAndValues.filterNot(_._1 == idx).map(_._2)
    )

  val valueChanged: ValueChanged => Unit = (v: ValueChanged) =>
    setStateWarnParent(
      identity[State],
      idsAndValues.map {
        case (v.id, _) => v.value
        case other     => other._2
      }
    )

  def render(): ReactElement = {

    assert(props.values.nonEmpty, "ListForm values can't be empty")

    val idOfLast = idsAndValues.last._1
    fieldset(
      props.title,
      ul(
        idsAndValues.map {
          case (id, value) =>
            li(key := id.toString)(
              Row(
                id,
                value,
                valueChanged,
                delete,
                props.maybePossibleChoices,
                add,
                isLast = idOfLast == id
              )
            )
        }
      )
    )
  }

}
