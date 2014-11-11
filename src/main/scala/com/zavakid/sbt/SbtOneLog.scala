package com.zavakid.sbt

import sbt.Keys._
import sbt._

/**
 *
 * @author zavakid 2014-11-10
 */
object OneLogKeys {

  val slf4jVersion = settingKey[String]("which slf4j version to use")
  val logbackVersion = settingKey[String]("which logback version to use")
  val scalaLoggingVersion = settingKey[String]("which scalaLogging version to use")
  val useScalaLogging = settingKey[Boolean]("add the scalaLogging(https://github.com/typesafehub/scala-logging)")
  val logbackXMLTemplate = settingKey[String]("the logback template path")
  val logbackTestXMLTemplate = settingKey[String]("the logback-test template path")
  val withLogDependencies = settingKey[Seq[sbt.ModuleID]]("with log dependencies")
  val generateLogbackXML = inputKey[Unit]("generate logback.xml and logback-test.xml if they are not exist")

}


object SbtOneLog extends AutoPlugin {


  import com.zavakid.sbt.OneLogKeys._

  val autoImport = OneLogKeys

  override def projectSettings: Seq[Setting[_]] = Seq[Setting[_]](
    slf4jVersion := "1.7.7"
    , logbackVersion := "1.1.2"
    , scalaLoggingVersion := "2.1.2"
    , useScalaLogging := true
    , resolvers += "99-empty" at "http://version99.qos.ch/"

  )


}
