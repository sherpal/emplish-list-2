package syntax

import scala.util.Try

trait FromString[T] {
  def fromString(x: String): T

  def tryFromString(x: String): Try[T] = Try(fromString(x))
  def optionFromString(x: String): Option[T] = tryFromString(x).toOption
}

object FromString {

  def apply[T](projection: String => T): FromString[T] = (x: String) => projection(x)

  implicit def stringFromString: FromString[String] = apply(identity)
  implicit def intFromString: FromString[Int] = apply(_.toInt)
  implicit def doubleFromString: FromString[Double] = apply(_.toDouble)

}
