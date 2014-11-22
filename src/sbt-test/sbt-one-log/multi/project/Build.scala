import sbt._
import sbt.Keys._
import com.zavakid.sbt._

object Build extends sbt.Build {

  val commonSettings = Defaults.defaultSettings ++
    Seq(
     scalaVersion := "2.10.3"
     ,version := "0.1"
  )

  lazy val root = Project(
    id = "multi",
    base = file("."),
    settings = commonSettings ++ Seq(
        // custom settings here
    )
  ).dependsOn(module1, module2)

  lazy val module1 = Project(
    id = "module1",
    base = file("module1"),
    settings = commonSettings ++ Seq(
      libraryDependencies := libraryDependencies.value ++ Seq(
        "org.mybatis" % "mybatis" % "3.2.7" //dependent slf4j-log4j12
        //,"commons-beanutils" % "commons-beanutils" % "1.9.1" //dependent commons-logging
        //, "com.alibaba.otter" % "node.deployer" % "4.2.11"
      )
    )
  ).enablePlugins(SbtOneLog)

  lazy val module2 = Project(
    id = "module2",
    base = file("module2"),
    settings = commonSettings ++ Seq(
      libraryDependencies := libraryDependencies.value ++ Seq(
        "commons-beanutils" % "commons-beanutils" % "1.9.1" //dependent commons-logging
        ,"org.apache.commons" % "commons-dbcp2" % "2.0.1"
      )
    )
  ).enablePlugins(SbtOneLog)
}
