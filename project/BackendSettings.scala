import sbt._
import sbt.Def.settings
import sbt.Keys.libraryDependencies
import play.sbt.PlayImport._

object BackendSettings {

  def apply(): Seq[Def.Setting[_]] = settings(
    libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test,
    libraryDependencies ++= Seq(evolutions),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-slick" % "4.0.2",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.0",
      "org.xerial" % "sqlite-jdbc" % "3.28.0",
      "com.h2database" % "h2" % "1.4.199" % Test,
      "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0",
      // BCrypt library for hashing password
      "org.mindrot" % "jbcrypt" % "0.3m",
      // sending emails
      "javax.mail" % "mail" % "1.4.7",
      "org.postgresql" % "postgresql" % "42.2.5"
    )
    // https://www.playframework.com/documentation/2.7.x/SBTCookbook
    //PlayKeys.playRunHooks += baseDirectory.map(FrontendHook.apply).value
  )

}
