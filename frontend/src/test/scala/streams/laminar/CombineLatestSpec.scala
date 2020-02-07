package streams.laminar

import com.raquo.laminar.api.L._
import org.scalatest.{FlatSpec, Matchers}

final class CombineLatestSpec extends FlatSpec with Matchers {

  implicit val owner: Owner = new Owner {}

  "CombineLatest" should "emit when left and right emitted" in {

    val leftVar = Var(0)
    val rightVar = Var("")

    val left$ = leftVar.signal.changes
    val right$ = rightVar.signal.changes

    val combined = CombineLatest(left$, right$)

    var acc: List[(Int, String)] = Nil

    combined.foreach(acc :+= _)

    leftVar.writer.onNext(1)
    rightVar.writer.onNext("hello")

    acc should be(List((1, "hello")))

  }

  "CombineLatest" should "emit previous left value when right arrives" in {
    val leftVar = Var(0)
    val rightVar = Var("")

    val left$ = leftVar.signal.changes
    val right$ = rightVar.signal.changes

    val combined = CombineLatest(left$, right$)

    var acc: List[(Int, String)] = Nil

    combined.foreach(acc :+= _)

    leftVar.writer.onNext(1)
    rightVar.writer.onNext("hello")
    rightVar.writer.onNext("bar")

    acc should be(List(1 -> "hello", 1 -> "bar"))
  }

}
