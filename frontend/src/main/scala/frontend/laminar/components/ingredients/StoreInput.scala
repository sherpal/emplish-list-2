package frontend.laminar.components.ingredients

import frontend.laminar.components.forms.FormGroup
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.helpers.InputSearch
import models.emplishlist.Store
import org.scalajs.dom
import org.scalajs.dom.html

final class StoreInput(stores: List[Store], val writer: Observer[Store], val events: Observable[Store])
    extends FormGroup[Store, dom.html.Span] {
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

  def apply(stores: List[Store], events: Observable[Store], writer: Observer[Store]): StoreInput =
    new StoreInput(stores, writer, events)

}
