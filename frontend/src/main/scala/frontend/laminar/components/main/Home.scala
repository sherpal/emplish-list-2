package frontend.laminar.components.main

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontend.laminar.components.Component
import org.scalajs.dom
import org.scalajs.dom.html.Div

final class Home private () extends Component[dom.html.Div] {
  val element: ReactiveHtmlElement[Div] = div(
    h1("Welcome to Emplish List!"),
    p("You can add new ingredients via the 'Ingredients' tab, and add recipes with the 'Recipes' tab."),
    p("You can then create your list of to go for your shopping in the 'Create list' tab."),
    p(
      """Recipes and ingredients can have 'tags' associated to them, in order to find them more easily. For example,
        |you can put tags like "dessert" or "dejeuner".
        |""".stripMargin
    ),
    p(
      "Each ingredients can be attached to stores in which we can find them. New stores need to be added manually by" +
        " myself."
    ),
    p(
      """
        |By convention, we'll put names of ingredients and recipes in french... The tags can only be in lower case and
        |only using the alphabet a-z.
        |""".stripMargin
    )
  )
}

private[main] object Home {
  def apply(): Home = new Home
}
