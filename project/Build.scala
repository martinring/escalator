import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
  val appName         = "escalator"
  val appVersion      = "1.0-SNAPSHOT"
  
  val appDependencies = Seq(
    "org.scala-lang" % "scala-compiler" % "2.10.0",
    "org.webjars" %% "webjars-play" % "2.1.0-2",
    "org.webjars" % "jquery" % "2.0.3",
    "org.webjars" % "codemirror" % "3.14"
  )
  
  val main = play.Project(appName, appVersion, appDependencies).settings(
    
  )
}
