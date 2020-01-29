package frontend.laminar.components.ingredients

import com.raquo.laminar.api.L
import frontend.laminar.components.forms.FormGroup
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.helpers.InputSearch
import models.emplishlist.Store
import org.scalajs.dom
import org.scalajs.dom.html

final class StoreInput(
    index: Int,
    $storesWithIndex: Observable[(Store, Int)],
    storesWithIndexWriter: Observer[(Store, Int)],
    stores: List[Store]
) extends FormGroup[Store, dom.html.Span] {

  val events: L.Observable[Store] = $storesWithIndex.map(_._1)
  val writer: Observer[Store] = storesWithIndexWriter.contramap(_ -> index)

  val element: ReactiveHtmlElement[html.Span] = span(
    "Store: ",
    InputSearch[Store](
      events,
      stores,
      _ => _ => true,
      name => stores.find(_.name == name).getOrElse(Store(0, name)),
      _.name,
      writer
    )
  )
}

object StoreInput {

  def apply(
      index: Int,
      events: Observable[(Store, Int)],
      writer: Observer[(Store, Int)],
      stores: List[Store]
  ): StoreInput =
    new StoreInput(index, events, writer, stores)

}
