package syntax

import cats.kernel.Monoid

import scala.language.implicitConversions

trait WithUnit[T] {
  def unit: T

  def empty: T = unit // compatibility with Cats monoids
}

object WithUnit {

  def apply[T](value: T): WithUnit[T] = new WithUnit[T] {
    def unit: T = value
  }

  implicit def monoidIsWithUnit[T](implicit monoid: Monoid[T]): WithUnit[T] = new WithUnit[T] {
    def unit: T = monoid.empty
  }

  implicit def pairUnit[A, B](implicit unitA: WithUnit[A], unitB: WithUnit[B]): WithUnit[(A, B)] =
    apply((unitA.unit, unitB.unit))

  def invoke[T](implicit withUnit: WithUnit[T]): WithUnit[T] = withUnit

}
