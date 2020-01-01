package frontend.components.helpers.lab

import frontend.components.helpers.lab.ChangingRandomSpan.Def
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.{React, ReactElement, ReactRef}
import slinky.web.html._

@react final class TestUsingRef extends StatelessComponent {

  type Props = Unit

  val myRef: ReactRef[Def] = React.createRef[ChangingRandomSpan.Def]

  def render(): ReactElement = div(
    button(onClick := (() => println(myRef.current.state.underlyingSpan.map(_.innerHTML))))("Click!"),
    ChangingRandomSpan().withRef(myRef)
  )

}
