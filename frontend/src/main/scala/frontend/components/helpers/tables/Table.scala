package frontend.components.helpers.tables
import slinky.core.facade.ReactElement
import slinky.web.html._

object Table {

  def apply[T](
      elements: List[T],
      header: Option[ReactElement],
      row: (T, String) => ReactElement,
      tKey: T => String
  ): ReactElement = table(
    thead(header),
    tbody(
      elements.map(elem => row(elem, tKey(elem)))
    )
  )

}
