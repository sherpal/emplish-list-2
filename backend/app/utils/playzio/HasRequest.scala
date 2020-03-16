package utils.playzio

import scala.language.higherKinds

trait HasRequest[R[_], A] {
  def request: R[A]
}

object HasRequest {
  def apply[R[_], A](r: R[A]): HasRequest[R, A] = new HasRequest[R, A] {
    def request: R[A] = r
  }
}
