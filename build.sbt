import sbt.Keys._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

name := "EmplishList"

version := "0.1"

scalaVersion := "2.12.10"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Ywarn-unused:locals"
)

lazy val `shared` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    SharedSettings(),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)//,
    //scalacOptions
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
    libraryDependencies += guice
  )
  .dependsOn(sharedJVM)

/** Frontend will use react with Slinky */
lazy val `frontend` = (project in file("./frontend"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(FrontendSettings())
  .dependsOn(sharedJS)
//  .settings( // example library to make http calls
//    resolvers += Resolver.bintrayRepo("hmil", "maven"),
//    libraryDependencies += "fr.hmil" %%% "roshttp" % "2.2.4"
//  )

addCommandAlias("dev", ";frontend/fastOptJS::startWebpackDevServer;~frontend/fastOptJS")

addCommandAlias("build", "frontend/fullOptJS::webpack")
