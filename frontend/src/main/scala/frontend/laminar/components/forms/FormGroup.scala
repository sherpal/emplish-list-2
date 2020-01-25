package frontend.laminar.components.forms

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import org.scalajs.dom

import scala.language.implicitConversions

/**
  * A [[FormGroup]] is meant to be a (re)usable piece of form that represent some data of type T.
  *
  * The FormGroup displays the elements coming from the `events` and outputs elements sent to the `writer`.
  *
  * This class doesn't do anything for you, so it's basically only semantic in your program, meaning that a
  * [[Component]] that is also a [[FormGroup]] receives and output data, and is meant to be inside a form.
  */
trait FormGroup[T, Ref <: dom.html.Element] extends Component[Ref] {

  def writer: Observer[T]
  def events: Observable[T]

}

object FormGroup {

  implicit def asElement[Ref <: dom.html.Element](formGroup: FormGroup[_, Ref]): ReactiveHtmlElement[Ref] =
    formGroup.element
}
