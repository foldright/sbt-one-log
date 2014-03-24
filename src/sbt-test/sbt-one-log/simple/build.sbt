import OneLogKeys._

version := "0.1"

scalaVersion := "2.10.3"

oneLogSettings

slf4jVersion := "1.7.5"

logbackVersion := "1.0.3"

val expected = Set[String](
  "org.slf4j:log4j-over-slf4j:1.7.5"
  ,"org.slf4j:jcl-over-slf4j:1.7.5"
  ,"org.slf4j:jul-to-slf4j:1.7.5"
  ,"org.slf4j:slf4j-api:1.7.5"
  ,"ch.qos.logback:logback-classic:1.0.3"
  ,"ch.qos.logback:logback-core:1.0.3"
  ,"commons-logging:commons-logging:99-empty"
  ,"commons-logging:commons-logging-api:99-empty"
  ,"log4j:log4j:99-empty"
)

TaskKey[Unit]("check") <<= (libraryDependencies) map { deps =>
  if(!expected.subsetOf(deps.map(_.toString).toSet))
    error("libraryDependencies error!")
  ()
}
