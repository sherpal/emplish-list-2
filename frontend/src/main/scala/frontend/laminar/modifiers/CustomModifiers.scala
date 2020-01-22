package frontend.laminar.modifiers

import com.raquo.domtypes.generic.builders.PropBuilder
import com.raquo.domtypes.generic.codecs.Codec
import com.raquo.laminar.keys.ReactiveProp

object CustomModifiers extends PropBuilder[ReactiveProp] {
  protected def prop[V, DomV](key: String, codec: Codec[V, DomV]): ReactiveProp[V, DomV] = ???
}
