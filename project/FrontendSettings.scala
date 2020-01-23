import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Def.settings
import sbt.Keys.{baseDirectory, libraryDependencies, resolvers, scalacOptions, version}
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object FrontendSettings {

  def apply(): Seq[Def.Setting[_]] = settings(
    npmDependencies in Compile += "react" -> "16.8.6",
    npmDependencies in Compile += "react-dom" -> "16.8.6",
    npmDependencies in Compile += "react-proxy" -> "1.1.8",
    npmDependencies in Compile += "react-router" -> "5.1.2",
    npmDependencies in Compile += "react-router-dom" -> "5.1.2",
    npmDependencies in Compile += "history" -> "4.10.1",
    npmDevDependencies in Compile += "file-loader" -> "3.0.1",
    npmDevDependencies in Compile += "style-loader" -> "0.23.1",
    npmDevDependencies in Compile += "css-loader" -> "2.1.1",
    npmDevDependencies in Compile += "html-webpack-plugin" -> "3.2.0",
    npmDevDependencies in Compile += "copy-webpack-plugin" -> "5.0.2",
    npmDevDependencies in Compile += "webpack-merge" -> "4.2.1",
    libraryDependencies += "me.shadaj" %%% "slinky-web" % "0.6.3",
    libraryDependencies += "me.shadaj" %%% "slinky-hot" % "0.6.3",
    libraryDependencies += "me.shadaj" %%% "slinky-native" % "0.6.3",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.5" % Test,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    version in webpack := "4.29.6",
    version in startWebpackDevServer := "3.2.1",
    webpackResources := baseDirectory.value / "webpack" * "*",
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack" / "webpack-fastopt.config.js"),
    webpackConfigFile in fullOptJS := Some(baseDirectory.value / "webpack" / "webpack-opt.config.js"),
    webpackConfigFile in Test := Some(baseDirectory.value / "webpack" / "webpack-core.config.js"),
    webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot"),
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
    requireJsDomEnv in Test := true,
    // akka
    libraryDependencies += "org.akka-js" %%% "akkajsactor" % "1.2.5.23",
    libraryDependencies += "org.akka-js" %%% "akkajsactorstream" % "1.2.5.23",
    libraryDependencies += "org.akka-js" %%% "akkajstestkit" % "2.2.6.1" % "test",
    libraryDependencies += "org.akka-js" %%% "akkajsstreamtestkit" % "2.2.6.1" % "test",
    // laminar
    libraryDependencies += "com.raquo" %%% "laminar" % "0.7.2",
    scalaJSLinkerConfig in (Compile, fullOptJS) ~= { _.withClosureCompiler(false) }
  )

}
