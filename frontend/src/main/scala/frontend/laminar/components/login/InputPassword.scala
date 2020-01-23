package frontend.laminar.components.login

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import models.errors.BackendError
import org.scalajs.dom.html

object InputPassword {

  def apply(
      title: String,
      $changeFormData: Observer[String],
      $errors: EventStream[Map[String, List[BackendError]]]
  ): ReactiveHtmlElement[html.Div] =
    div(
      className := "input-string",
      title,
      input(
        tpe := "password",
        inContext(thisNode => onInput.mapTo(thisNode.ref.value) --> $changeFormData)
      ),
      div(
        "Password and confirmation should match",
        display <-- $errors.map(_.keys.toSet.contains("passwordMatch"))
          .map(if (_) "block" else "none")
          .fold("none")((_, newElem) => newElem)
      )
    )

}
