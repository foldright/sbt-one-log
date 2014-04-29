package com.zavakid.sbt

import sbt._
import sbt.Keys._
import complete._
import complete.DefaultParsers._

/**
 * Created by ZavaKid on 2014-03-24
 */
object OneLog extends Plugin {

  object OneLogKeys {

    val slf4jVersion = settingKey[String]("which slf4j version to use")
    val logbackVersion = settingKey[String]("which logback version to use")
    val scalaLoggingVersion = settingKey[String]("which scalaLogging version to use")
    val useScalaLogging = settingKey[Boolean]("add the scalaLogging(https://github.com/typesafehub/scala-logging)")

    val withLogDependencies = settingKey[Seq[sbt.ModuleID]]("with log dependencies")

    val generateLogbackXML = inputKey[Unit]("generate logback.xml and logback-test.xml if they are not exist")

    lazy val oneLogResolvers = Seq(
      "99-empty" at "http://version99.qos.ch/"
    )


    lazy val logs: Def.Initialize[Seq[ModuleID]] = Def.setting {
      val scalaLoggingDeps = if (useScalaLogging.value)
        Seq(
          "com.typesafe.scala-logging" % s"scala-logging-slf4j_${scalaBinaryVersion.value}"
            % scalaLoggingVersion.value force()
        )
      else Seq.empty

      scalaLoggingDeps ++ Seq(
        "org.slf4j" % "slf4j-api" % slf4jVersion.value force()
        , "org.slf4j" % "log4j-over-slf4j" % slf4jVersion.value force()
        , "org.slf4j" % "jcl-over-slf4j" % slf4jVersion.value force()
        , "org.slf4j" % "jul-to-slf4j" % slf4jVersion.value force()
        , "ch.qos.logback" % "logback-classic" % logbackVersion.value force()
        , "ch.qos.logback" % "logback-core" % logbackVersion.value force()
        , "commons-logging" % "commons-logging" % "99-empty" force()
        , "commons-logging" % "commons-logging-api" % "99-empty" force()
        , "log4j" % "log4j" % "99-empty" force()
      )
    }

    val generateLogbackXMLParser: Def.Initialize[Parser[Boolean]] = Def.setting {
      token(Space ~> "force").?.map(_.fold(false)(_ == "force"))
    }

    val oneLogSettings = Seq[Setting[_]](
      slf4jVersion := "1.7.7"
      , logbackVersion := "1.1.2"
      , scalaLoggingVersion := "2.1.2"
      , useScalaLogging := true
      , resolvers ++= oneLogResolvers
      , libraryDependencies ++= logs.value
      , libraryDependencies <<= libraryDependencies {
        deps =>
          deps.filter(logFilter).map(exclusionUnlessLog)
      },
      generateLogbackXML := {
        val force = generateLogbackXMLParser.parsed
        println("result: " + force)
      }
    )

    private def logFilter(dep: sbt.ModuleID): Boolean =
      if (exclusionLogs.contains((dep.organization, dep.name))) false
      else true

    private def exclusionUnlessLog(dep: sbt.ModuleID): sbt.ModuleID =
      dep.excludeAll(exclusionLogs: _*)


    private lazy val exclusionLogs = Seq(
      "org.slf4j" -> "slf4j-log4j12",
      "org.slf4j" -> "slf4j-jcl",
      "org.slf4j" -> "slf4j-jdk14"
    )

    private implicit def tuple2ExclusionRule(tuples: Seq[(String, String)]): Seq[ExclusionRule] =
      tuples.map(t => ExclusionRule(t._1, t._2)
      )
  }

}
