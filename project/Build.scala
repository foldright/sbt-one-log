
import sbt._
import Keys._


object OneLogBuild extends Build{

  val SCALA_VERSION = "2.10.3"

  lazy val buildSettings = Defaults.defaultSettings ++ Seq[Setting[_]](
    organization := "com.zavakid.sbt",
    organizationName := "one-log",
    organizationHomepage := Some(new URL("http://www.zavakid.com/sbt-one-log/")),
    description := "A sbt plugin for uniform log lib", 
    scalaVersion := SCALA_VERSION,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    //publishTo
    sbtPlugin := true,
    crossPaths := false,
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-target:jvm-1.6")
  )

  lazy val oneLog = Project(
    id = "one-log",
    base = file("."),
    settings = buildSettings ++ Seq(libraryDependencies ++= (
      Seq("org.slf4j" % "slf4j-nop" % "1.7.5")
    ))
  )
}
