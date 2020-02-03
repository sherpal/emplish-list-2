package frontend.laminar.components.helpers

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import org.scalajs.dom

final class InputTags($tags: Observable[List[String]], tagsWriter: Observer[List[String]])
    extends Component[dom.html.Input] {

  val element: ReactiveHtmlElement[dom.html.Input] = input(
    `type` := "text",
    value <-- $tags.map(_.map("#" + _).mkString(" ")),
    inContext(
      thisInput =>
        onChange
          .mapTo(thisInput.ref.value)
          .map(_.split(" ").toList.filter(_.startsWith("#")).map(_.tail)) --> tagsWriter
    ),
    width := "100%"
  )
}

object InputTags {
  def apply($tags: Observable[List[String]], tagsWriter: Observer[List[String]]): InputTags = new InputTags(
    $tags,
    tagsWriter
  )
}
