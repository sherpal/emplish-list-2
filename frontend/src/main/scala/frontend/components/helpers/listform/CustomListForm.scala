package frontend.components.helpers.listform

import frontend.{NewItemPictogram, TrashPictogram}
import slinky.core.facade.ReactElement
import slinky.web.html._
import syntax.WithUnit

object CustomListForm {

  trait Row[T] {
    def render(index: Int, t: T, onUpdate: (Int, T) => Unit): ReactElement
  }

  def apply[T](
      title: String,
      idsAndValues: List[(Int, T)],
      row: () => Row[T],
      changeState: List[(Int, T)] => Unit
  )(implicit withUnit: WithUnit[T]): ReactElement = {

    def ids = idsAndValues.map(_._1)

    def nextId = if (ids.isEmpty) 0 else ids.max + 1

    val valueChanged: (Int, T) => Unit = (id: Int, elem: T) =>
      changeState(
        idsAndValues.map {
          case (idx, _) if idx == id => (id, elem)
          case (idx, e)              => (idx, e)
        }
      )

    fieldset(
      title,
      ul(
        idsAndValues.map {
          case (id, elem) =>
            li(key := id.toString)(
              row().render(id, elem, valueChanged),
              " ",
              span(className := "clickable", onClick := (() => changeState(idsAndValues.filterNot(_._1 == id))))(
                img(src := TrashPictogram.asInstanceOf[String], alt := "delete", className := "icon-size")
              )
            )
        }
      ),
      span(
        className := "clickable",
        onClick := (() => changeState(idsAndValues :+ (nextId, withUnit.empty))),
        img(src := NewItemPictogram.asInstanceOf[String], alt := "new item", className := "icon-size")
      )
    )

  }

}
