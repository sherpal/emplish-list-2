package tasks

import akka.actor.ActorSystem
import javax.inject.Inject
import models.database.Users
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import utils.monix.SchedulerProvider
import zio.Runtime

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * This task will be launched at the start of play to add the admin user if it is not yet in the database.
  */
final class AddAdminInDBIfNotExists @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
    actorSystem: ActorSystem,
    executionContext: ExecutionContext
) extends SchedulerProvider
    with HasDatabaseConfigProvider[JdbcProfile] {

  val ec: ExecutionContext = executionContext

  lazy val log: Logger = Logger("AddAdminInDB")

  actorSystem.scheduler.scheduleOnce(delay = 3.seconds) {

    Runtime.default.unsafeRun(
      Users.registerAdminIfNotExist.orDie
        .map(_ => log.info("Admin user set up successfully"))
        .provideLayer(utils.config.Configuration.live ++ (models.database.dbProvider(db) >>> Users.live))
    )

  }

}
