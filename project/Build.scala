package com.zavakid.sbt

import sbt._
import Keys._
import sbt.ScriptedPlugin._


object OneLogBuild extends Build{

  val SCALA_VERSION = "2.10.3"

  lazy val buildSettings = Defaults.defaultSettings ++ scriptedSettings ++ Seq[Setting[_]](
    organization := "com.zavakid.sbt",
    organizationName := "sbt-one-log",
    organizationHomepage := Some(new URL("http://www.zavakid.com/sbt-one-log/")),
    description := "A sbt plugin for uniform log lib", 
    scalaVersion := SCALA_VERSION,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    //publishTo
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= {
       import scala.collection.JavaConverters._
       val opts = management.ManagementFactory.getRuntimeMXBean().getInputArguments().asScala
       val filtered = opts.filter(opt => Seq("-Xmx","-Xms").contains(opt) || opt.startsWith("-XX"))
       filtered.toSeq
    },
    sbtPlugin := true,
    crossPaths := false,
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-target:jvm-1.6"),
    pomExtra := {
      <url>http://www.zavakid.com/sbt-one-log/</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com:zavakid/sbt-one-log.git</connection>
        <developerConnection>scm:git:git@github.com:zavakid/sbt-one-log.git</developerConnection>
        <url>github.com/zavakid/sbt-one-log</url>
      </scm>
      <developers>
        <developer>
          <id>zava</id>
          <name>Zavakid</name>
          <url>http://www.zavakid.com</url>
        </developer>
      </developers>
    }
  )

  lazy val oneLog = Project(
    id = "sbt-one-log",
    base = file("."),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq("org.slf4j" % "slf4j-nop" % "1.7.5")
    )
  )
}
