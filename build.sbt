import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.1.0"
val meanjsVersion = "0.1.14"
val _scalaVersion = "2.11.8"

val paradisePluginVersion = "3.0.0-M1"
val scalaJsDomVersion = "0.9.0"
val scalaJsJQueryVersion = "0.9.0"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

val jsCommonSettings = Seq(
  scalaVersion := _scalaVersion,
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq(
    "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
  ),
  relativeSourceMaps := true,
  persistLauncher := true,
  persistLauncher in Test := false,
  homepage := Some(url("https://github.com/meansjs/todo-mvc")),
  addCompilerPlugin("org.scalamacros" % "paradise" % paradisePluginVersion cross CrossVersion.full),
  ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
  libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % scalaJsJQueryVersion,
    "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
    "org.scala-lang" % "scala-reflect" % _scalaVersion
  )
)

lazy val commonjs = (project in file("app-commonjs"))
  .enablePlugins(ScalaJSPlugin)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "todo-mvc-commonjs",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "means-core" % meanjsVersion
    )
  )

lazy val angularjs = (project in file("app-angularjs"))
  .enablePlugins(ScalaJSPlugin)
  .aggregate(commonjs)
  .dependsOn(commonjs)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "todo-mvc-angularjs",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "means-angularjs-core" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-angularjs-toaster" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-angularjs-ui-router" % meanjsVersion
    )
  )

lazy val nodejs = (project in file("app-nodejs"))
  .aggregate(commonjs)
  .dependsOn(commonjs, angularjs)
  .enablePlugins(ScalaJSPlugin)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "todo-mvc-nodejs",
    organization := "com.github.ldaniels528",
    version := appVersion,
    Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(angularjs, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    },
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in(angularjs, Compile)),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "means-bundle-mean-minimal" % meanjsVersion
    )
  )

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project nodejs", _: State)) compose (onLoad in Global).value
