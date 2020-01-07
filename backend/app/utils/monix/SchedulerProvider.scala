package utils.monix

import java.util.concurrent.Executors

import monix.execution.ExecutionModel.AlwaysAsyncExecution
import monix.execution.UncaughtExceptionReporter
import monix.execution.schedulers.AsyncScheduler

import scala.concurrent.ExecutionContext

trait SchedulerProvider {

  val ec: ExecutionContext

  implicit lazy val scheduler: AsyncScheduler = AsyncScheduler(
    Executors.newSingleThreadScheduledExecutor(),
    ec,
    AlwaysAsyncExecution,
    UncaughtExceptionReporter(ec.reportFailure)
  )

}
