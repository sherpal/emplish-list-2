import com.heroku.sbt.HerokuPlugin.autoImport.{herokuIncludePaths, herokuProcessTypes}
import sbt.Keys._
import sbtassembly.MergeStrategy
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

name := "EmplishList"

version := "0.2"

scalaVersion := "2.12.10"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

lazy val `shared` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .disablePlugins(HerokuPlugin)
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .settings(
    SharedSettings(),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  )
  .jvmSettings(
    SharedSettings.jvmSettings
  )

lazy val sharedJVM = `shared`.jvm
lazy val sharedJS = `shared`.js

/** Backend server uses Play framework */
lazy val `backend` = (project in file("./backend"))
  .enablePlugins(PlayScala)
  .settings(
    BackendSettings(),
    libraryDependencies += guice,
    herokuAppName in Compile := "emplish-list",
    herokuProcessTypes in Compile := Map(
      "web" -> "target/universal/stage/bin/backend -Dhttp.port=$PORT" //,
      //"worker" -> "java -jar target/universal/stage/lib/my-worker.jar"
    ),
    herokuIncludePaths in Compile := Seq(
      "backend/app",
      "backend/conf/routes",
      "backend/conf/application.conf",
      "backend/public"
    ),
    herokuSkipSubProjects in Compile := false
  )
  .dependsOn(sharedJVM)

/** Frontend will use react with Slinky */
lazy val `frontend` = (project in file("./frontend"))
  .disablePlugins(HerokuPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .settings(FrontendSettings())
  .dependsOn(sharedJS)

addCommandAlias("dev", ";frontend/fastOptJS::startWebpackDevServer;~frontend/fastOptJS")

addCommandAlias("build", "frontend/fullOptJS::webpack")

stage := {
  val webpackValue = (frontend / Compile / fullOptJS / webpack).value

  println(s"Webpack value is $webpackValue")

  (stage in backend).value
}
//Compile / deployHeroku := (backend / Compile / deployHeroku).value

// sbt clean stage backend/deploy
