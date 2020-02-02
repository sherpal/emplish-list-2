package frontend.laminar.fixlaminar

import com.raquo.laminar.api.L._
import org.scalajs.dom

object Fixes {

  final val readMountEvents: Mod[HtmlElement] = new Mod[HtmlElement] {
    override def apply(element: HtmlElement): Unit = {
      element.ref.setAttribute("data-mountEventsTesting", "enabled") // just for debug convenience
      element.subscribe(_.mountEvents) { ev =>
        dom.console.log(ev)
      } // the actual payload
    }
  }

}
