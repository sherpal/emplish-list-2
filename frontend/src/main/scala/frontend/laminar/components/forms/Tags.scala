package frontend.laminar.components.forms

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html

object Tags {

  def tagsExplanation(kind: String): ReactiveHtmlElement[html.Element] = details(
    summary(s"Insert a list of tags for this $kind:"),
    p(
      """Prefix each tag by '#' and separate them by spaces (for example you can put "#winter #dessert")."""
    )
  )

  def tagsFilterExplanation(kind: String): ReactiveHtmlElement[html.Element] = details(
    summary("Tags filtering"),
    p(
      s"Below you can type in tags to filter ${kind}s and only display a subset."
    ),
    p(
      """Enter each tag prefixed by a '#' and separate them by a space (for example, "#bebe #dejeuner")."""
    ),
    p("Note that changes only occur after you release the input focus (e.g., by pressing Enter).")
  )

}
