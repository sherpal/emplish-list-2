package streams.laminar

import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.signal.Signal

object CombineLatest {

  /**
    * Combines two streams into a stream of pairs. The emitted values are the pair of last emitted values from both
    * incoming streams.
    *
    * Starts when: both streams emitted at least a value
    * Emits when: either stream emits a value
    * Ends when: both streams have ended.
    */
  def apply[L, R](left$ : EventStream[L], right$ : EventStream[R]): EventStream[(L, R)] =
    EventStream
      .merge(left$.map(Left[L, R]), right$.map(Right[L, R]))
      .fold[(Option[L], Option[R])]((Option.empty[L], Option.empty[R])) { (acc: (Option[L], Option[R]), value) =>
        value match {
          case Left(left)   => (Some(left), acc._2)
          case Right(right) => (acc._1, Some(right))
        }
      }
      .changes
      .collect { case (Some(left), Some(right)) => (left, right) }

  def apply[L, R](left$ : Signal[L], right$ : Signal[R], startLeft: => L, startRight: => R): Signal[(L, R)] =
    EventStream
      .merge(left$.changes.map(Left[L, R]), right$.changes.map(Right[L, R]))
      .fold((startLeft, startRight)) { (acc: (L, R), value) =>
        value match {
          case Left(left)   => (left, acc._2)
          case Right(right) => (acc._1, right)
        }
      }

}
