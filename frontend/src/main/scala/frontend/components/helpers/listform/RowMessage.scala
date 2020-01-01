package frontend.components.helpers.listform

private[listform] sealed trait RowMessage

object RowMessage {

  case class ValueChanged(id: Int, value: String)

}
