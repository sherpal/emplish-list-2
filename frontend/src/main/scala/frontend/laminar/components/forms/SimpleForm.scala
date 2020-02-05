package frontend.laminar.components.forms

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import com.raquo.airstream.eventbus.{EventBus, WriteBus}
import com.raquo.airstream.eventstream.EventStream
import com.raquo.laminar.api.L._
import models.errors.BackendError
import models.validators.FieldsValidator
import streams.sinks.WriteToObserver._
import streams.sources.ReadFromObservable._
import syntax.WithUnit

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait SimpleForm[FormData] {

  protected implicit val owner: Owner = new Owner {}

  type FormDataChanger = FormData => FormData

  implicit val formDataWithUnit: WithUnit[FormData]
  implicit val actorSystem: ActorSystem
  protected implicit def ec: ExecutionContext = actorSystem.dispatcher

  lazy val formData: Var[FormData] = Var(formDataWithUnit.unit) // lazy to avoid problems with missing formDataWithUnit
  private val formDataBus = new EventBus[FormData]()
  formDataBus.events.foreach(data => formData.update(_ => data))
  private val errors = new EventBus[Map[String, List[BackendError]]]()
  val $errors: EventStream[Map[String, List[BackendError]]] = errors.events // expose to kids
  val errorsWriter: WriteBus[Map[String, List[BackendError]]] = errors.writer // expose to kids

  private val formDataChanger: EventBus[FormDataChanger] = new EventBus[FormDataChanger]()
  private val formDataChangerWriter: WriteBus[FormDataChanger] = formDataChanger.writer

  /**
    * Allows to concretely make changes to the formData.
    */
  def createFormDataChanger[T](f: T => FormDataChanger): WriteBus[T] =
    formDataChangerWriter.contramapWriter(f)

  private val formDataEventWriter: WriteBus[FormData] = formDataBus.writer

  val validator: FieldsValidator[FormData, BackendError]

  private val formDataSink = Sink.writeToObserver(formDataEventWriter)
  private val errorsSink = Sink.foreach(errorsWriter.onNext)

  private val debugSink = Flow[FormData]
    .filter(_ => scala.scalajs.LinkingInfo.developmentMode)
    .to(
      Sink.foreach(println)
    )

  private val formSource: RunnableGraph[NotUsed] = Source
    .readFromObservable(formDataChanger.events)
    .scan(formDataWithUnit.unit) { case (form, changer) => changer(form) }
    .alsoTo(debugSink)
    .alsoTo(formDataSink)
    .groupedWithin(10, 200.milliseconds)
    .map(_.last)
    .map(validator.validate)
    .wireTap(errors => println(s"The errors pass: ${errors.size} errors"))
    .to(errorsSink)

  final def run(): Unit = formSource.run()

  final def clearForm(): Unit =
    formDataChangerWriter.onNext(_ => formDataWithUnit.unit)

  final def setFormData(data: FormData): Unit =
    formDataChangerWriter.onNext(_ => data)

}
