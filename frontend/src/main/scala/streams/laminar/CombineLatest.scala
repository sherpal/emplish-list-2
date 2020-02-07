package streams.laminar

import com.raquo.airstream.eventstream.EventStream

object CombineLatest {

  /**
    * Combines two streams into a stream of pairs. The emitted values are the pair of last emitted values from both
    * incoming streams.
    *
    * Starts when: both streams emitted at least a value
    * Emits when: either stream emits a value
    * Ends when: both streams have ended.
    */
  def apply[L, R](left$ : EventStream[L], right$ : EventStream[R]): EventStream[(L, R)] = {
    val leftAsLeft$ = left$.map(Left[L, R])
    val rightAsRight$ = right$.map(Right[L, R])

    val mergeEithers$ = EventStream.merge(leftAsLeft$, rightAsRight$)

    mergeEithers$
      .fold[(Option[L], Option[R])]((Option.empty[L], Option.empty[R])) { (acc: (Option[L], Option[R]), value) =>
        value match {
          case Left(left)   => (Some(left), acc._2)
          case Right(right) => (acc._1, Some(right))
        }
      }
      .changes
      .collect { case (Some(left), Some(right)) => (left, right) }
  }

}
