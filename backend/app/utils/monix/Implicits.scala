package utils.monix

import monix.eval.Task
import monix.execution.{CancelableFuture, Scheduler}
import play.api.mvc.{Result, Results}

object Implicits extends Results {

  final implicit class BooleanTaskEnhanced(task: Task[Boolean]) {
    def runToResult(implicit s: Scheduler): CancelableFuture[Result] =
      task.map(if (_) Ok else BadRequest).onErrorHandle(_ => InternalServerError).runToFuture
  }

}
