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
    )
  }

}
