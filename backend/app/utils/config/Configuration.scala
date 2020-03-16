package utils.config

import utils.config.ConfigRequester.{FromConfig, |>}
import zio.{UIO, URIO, ZIO, ZLayer}

object Configuration {

  trait Service {
    def load[T](configRequester: ConfigRequester)(implicit fromConfig: FromConfig[T]): UIO[T]

    def adminName: UIO[String]
    def adminPassword: UIO[String]
  }

  val live: ZLayer.NoDeps[Nothing, Configuration] = ZLayer.succeed(
    new Service {
      def load[T](configRequester: ConfigRequester)(implicit fromConfig: FromConfig[T]): UIO[T] =
        ZIO.succeed(configRequester.into[T])

      def adminName: UIO[String] = load[String](|> >> "adminUser" >> "name")

      def adminPassword: UIO[String] = load[String](|> >> "adminUser" >> "password")
    }
  )

  def load[T](configRequester: ConfigRequester)(implicit fromConfig: FromConfig[T]): URIO[Configuration, T] =
    ZIO.accessM(_.get.load(configRequester))

  def adminName: URIO[Configuration, String] = ZIO.accessM(_.get.adminName)
  def adminPassword: URIO[Configuration, String] = ZIO.accessM(_.get.adminPassword)

}
