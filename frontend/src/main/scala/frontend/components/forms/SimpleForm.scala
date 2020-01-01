package frontend.components.forms

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink, Source, SourceQueueWithComplete}
import akka.stream.{Materializer, OverflowStrategy}
import models.errors.BackendError
import models.validators.FieldsValidator
import syntax.WithUnit

import scala.concurrent.duration._

sealed trait SimpleForm[State, FormData] {

  /** Type alias to describe a change in the form data */
  type FormDataChanger = FormData => FormData

  /** Gets the list of errors this form contains. None if the form can be submitted */
  val validator: FieldsValidator[FormData, BackendError]

  /** State of the form at the beginning */
  def initialForm: FormData

  /** A Sink that will receive the FormData when they are transformed. */
  val formSink: Sink[FormData, _]

  /** A Sink that will receive the list of errors the form data contain. */
  val errorsSink: Sink[Map[String, List[BackendError]], _]

  /**
    * This queue allows children to add their inputs to the form of this [[SimpleForm]].
    *
    * The stream changes the state of this form in both ways:
    * - it keeps the form data up to date
    * - it keeps the list of errors up to date after validating.
    *
    * This [[SimpleForm]] is responsible to giving to children the possibility to add elements to
    * the queue.
    */
  lazy val formSource: RunnableGraph[SourceQueueWithComplete[FormDataChanger]] = Source
    .queue[FormDataChanger](Int.MaxValue, OverflowStrategy.dropNew)
    .scan(initialForm) { case (form, changer) => changer(form) }
    .alsoTo(formSink)
    .groupedWithin(10, 200.milliseconds)
    .map(_.last)
    .map(validator.validate)
    .toMat(errorsSink)(Keep.left)

  def run()(implicit actorSystem: ActorSystem, materializer: Materializer): SourceQueueWithComplete[FormDataChanger] =
    formSource.run()

  def sendErrors(keys: List[String], errors: Map[String, List[BackendError]]): List[BackendError] =
    keys.flatMap(errors.get).flatten

}

object SimpleForm {

  def apply[State, FormData](
      validator: FieldsValidator[FormData, BackendError],
      formSink: Sink[FormData, _],
      errorsSink: Sink[Map[String, List[BackendError]], _]
  )(implicit formDataUnit: WithUnit[FormData]): SimpleForm[State, FormData] = {
    val _validator = validator
    val _formSink = formSink
    val _errorsSinks = errorsSink

    new SimpleForm[State, FormData] {
      val validator: FieldsValidator[FormData, BackendError] = _validator

      def initialForm: FormData = formDataUnit.unit

      val formSink: Sink[FormData, _] = _formSink
      val errorsSink: Sink[Map[String, List[BackendError]], _] = _errorsSinks
    }
  }

}
