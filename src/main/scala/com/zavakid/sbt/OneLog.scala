package com.zavakid.sbt

import sbt._
import sbt.Keys._

/**
 * Created by ZavaKid on 2014-03-24
 */
object OneLog extends Plugin {

  object OneLogKeys {

    val slf4jVersion = settingKey[String]("which slf4j version to use")
    val logbackVersion = settingKey[String]("which logback version to use")

    val withLogDependencies = settingKey[Seq[sbt.ModuleID]]("with log dependencies")

    lazy val oneLogResolvers = Seq(
      "99-empty" at "http://version99.qos.ch/"
    )


    lazy val logs: Def.Initialize[Seq[ModuleID]] = Def.setting {
      Seq(
        "org.slf4j" % "log4j-over-slf4j" % slf4jVersion.value
        , "org.slf4j" % "jcl-over-slf4j" % slf4jVersion.value
        , "org.slf4j" % "jul-to-slf4j" % slf4jVersion.value
        , "org.slf4j" % "slf4j-api" % slf4jVersion.value
        , "ch.qos.logback" % "logback-classic" % logbackVersion.value
        , "ch.qos.logback" % "logback-core" % logbackVersion.value
        , "commons-logging" % "commons-logging" % "99-empty"
        , "commons-logging" % "commons-logging-api" % "99-empty"
        , "log4j" % "log4j" % "99-empty"
      )
    }

    val oneLogSettings = Seq[Setting[_]](
      slf4jVersion := "1.7.6"
      , logbackVersion := "1.1.1"
      , resolvers ++= oneLogResolvers
      , libraryDependencies ++= logs.value
      , libraryDependencies <<= libraryDependencies {
        deps =>
          deps.filter(logFilter).map(exclusionUnlessLog)
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
