package frontend.laminar.components.forms

import com.raquo.airstream.eventbus.{EventBus, WriteBus}
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.{NewItemPictogram, TrashPictogram}
import frontend.laminar.components.Component
import org.scalajs.dom
import org.scalajs.dom.html.FieldSet
import syntax.WithUnit

/**
  * Creates a field set for a form allowing the user to enter an arbitrary amount of elements of type T.
  *
  * @param title      title to give to the field set
  * @param $lists     signal of lists of elements of type T
  * @param listWriter writer to which write the changes to the list
  * @param row        define how to allow the user to fill the information for one instance of T. It has to be created
  *                   from an Observable, that will contain the current value of T, and an Observer to which feed the
  *                   new element of type T.
  * @param withUnit   unit for type T, in order to add new elements.
  */
final class ListForm[T](
    title: String,
    $lists: Signal[List[T]],
    listWriter: WriteBus[List[T]],
    row: (Observable[T], Observer[T]) => FormGroup[T, dom.html.Span]
)(implicit withUnit: WithUnit[T])
    extends Component[FieldSet] {

  implicit private val owner: Owner = new Owner {}

  val element: ReactiveHtmlElement[FieldSet] = {

    def valueUpdater(elements: List[(T, Int)])(maybeT: Option[T], index: Int): List[(T, Int)] = maybeT match {
      case Some(t) =>
        // update element in the list
        elements.map {
          case (_, j) if j == index => t -> index
          case (tt, idx)            => tt -> idx
        }
      case None =>
        // deleting element from the list
        elements.filterNot(_._2 == index)
    }

    val $rows = $lists.map(_.zipWithIndex)
      .map { ls =>
        ls.map {
          case (t, index) =>
            (t, index, valueUpdater(ls)(_, _))
        }
      }
      .map {
        _.map {
          case (t, index, updater) =>
            val observable = Val(t)
            val eventBus = new EventBus[(Option[T], Int)]()
            eventBus.events.map(updater.tupled).map(_.map(_._1)).foreach(listWriter.onNext)

            val observer = eventBus.writer.contramapWriter((tt: T) => (Some(tt), index))

            li(
              row(observable, observer),
              " ",
              span(
                className := "clickable",
                onClick.mapTo(updater(None, index)).map(_.map(_._1)) --> listWriter,
                img(src := TrashPictogram.asInstanceOf[String], alt := "delete", className := "icon-size")
              )
            )
        }
      }

    fieldSet(
      title,
      children <-- $rows,
      child <-- $lists.map(
        list =>
          span(
            className := "clickable",
            onClick.mapTo(list :+ withUnit.empty) --> listWriter,
            img(src := NewItemPictogram.asInstanceOf[String], alt := "new item", className := "icon-size")
          )
      )
    )
  }
}

object ListForm {

  def apply[T](
      title: String,
      $lists: Signal[List[T]],
      listWriter: WriteBus[List[T]],
      row: (Observable[T], Observer[T]) => FormGroup[T, dom.html.Span]
  )(implicit withUnit: WithUnit[T]): ListForm[T] = new ListForm(
    title,
    $lists,
    listWriter,
    row
  )

}
