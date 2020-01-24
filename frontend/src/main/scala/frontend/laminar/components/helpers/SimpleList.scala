package frontend.laminar.components.helpers

import com.raquo.laminar.api.L._
import com.raquo.laminar.builders.ReactiveHtmlTag
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom

import scala.collection.immutable

object SimpleList {

  /**
    * Displays the elements as li elements inside a li, with a h1 title and inside the enclosing element.
    */
  def apply[T, Ref <: dom.html.Element](
      title: String,
      elements: immutable.Seq[T],
      printer: T => String,
      enclosing: ReactiveHtmlTag[Ref]
  ): ReactiveHtmlElement[Ref] = enclosing(h1(title), ul(elements.map(printer).map(li(_))))

}
