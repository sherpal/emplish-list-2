package frontend.components.helpers.listform

import frontend.components.helpers.inputsearch.InputSearch
import frontend.components.helpers.listform.RowMessage.ValueChanged
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react final class Row extends StatelessComponent {

  case class Props(
      id: Int, // unique id to communicate with the parent
      currentValue: String, // remembers the value
      valueChanged: ValueChanged => Unit, // will change the values in the parent's list
      delete: Int => Unit, // will remove this element from the parent's list
      maybePossibleValues: Option[List[String]], // if defined, the list of possible values for this input
      add: () => Unit, // tell the parent to add new item
      isLast: Boolean // only the last in the list has the "add" button
  )

  def valueChanged(str: String): ValueChanged = ValueChanged(props.id, str)

  def valid: Option[Boolean] = props.maybePossibleValues.map(_.contains(props.currentValue))

  private def inputValue: ReactElement = props.maybePossibleValues match {
    case Some(possibleValues) =>
      InputSearch(
        props.currentValue,
        possibleValues,
        x => _.toLowerCase.contains(x.toLowerCase),
        s => props.valueChanged(valueChanged(s)),
        isValid = valid.map(v => (_: String) => v)
      )
    case None =>
      input(
        className := valid.map(if (_) "valid" else "invalid"),
        onChange := (event => props.valueChanged(valueChanged(event.target.value)))
      )
  }

  def render(): ReactElement = span(
    inputValue,
    if (props.isLast) AddRow(props.add) else span(),
    DeleteRow(() => props.delete(props.id))
  )

}
