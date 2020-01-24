package frontend.laminar.components.forms

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{RunnableGraph, Sink, Source}
import com.raquo.airstream.eventbus.WriteBus
import com.raquo.airstream.eventstream.EventStream
import models.errors.BackendError
import models.validators.FieldsValidator
import streams.sinks.WriteToBus._
import streams.sources.ReadFromEventStream._
import syntax.WithUnit

import scala.concurrent.duration._

final class SimpleForm[FormData](
    $formDataChanger: EventStream[FormData => FormData],
    formDataEventWriter: WriteBus[FormData],
    errorsEventWriter: Option[WriteBus[Map[String, List[BackendError]]]],
    val validator: FieldsValidator[FormData, BackendError]
)(implicit formDataWithUnit: WithUnit[FormData], materializer: Materializer) {

  type FormDataChanger = FormData => FormData

  private val formDataSink = Sink.writeToBus(formDataEventWriter)
  private val errorsSink = errorsEventWriter match {
    case Some(eventWriter) => Sink.foreach(eventWriter.onNext)
    case None              => Sink.ignore
  }

  private val formSource: RunnableGraph[NotUsed] = Source
    .readFromEventStream($formDataChanger)
    .scan(formDataWithUnit.unit) { case (form, changer) => changer(form) }
    .wireTap(formData => println("The form data passes: " + formData)) // todo: remove that
    .alsoTo(formDataSink)
    .filter(_ => errorsEventWriter.isDefined) // no validation if not required
    .groupedWithin(10, 200.milliseconds)
    .map(_.last)
    .map(validator.validate)
    .to(errorsSink)

  def run(): Unit = formSource.run()

}
