# sbt-one-log plugin
sbt-one-log is a sbt plugin make log dependency easy.

## features
* uniform your log dependency, current support slf4j and logback, other log lib will bridge to slf4j.

## why sbt-one-log
Scala can leverage lots of perfect Java lib, but it's chaotic with the Log libs in Java world.
looking at the Log lib below:

* java.util.logging
* commons-logging
* commons-logging-api
* log4j
* slf4j
* logback
* log4j 2
* scala-logging
* slf4s (latest version only support Scala 2.9.1)
* Grizzled SLF4J
* AVSL
* loglady
* logula (abandoned)

of course, you can keep your project dependency cleanly with one or two log lib (e.g. slf4j and logback)

but sometimes your other dependency is out of control. 
e.g. if you dependency with Apache httpclient lib which dependeny with commons-logging, you will log with commons-logging

also, you can add jcl-over-slf4j and exclud commons-logging explicitly in libraryDependencies setting.
A better way is to explicitly declare dependency commons-logging with the sepcial version 99-empty.

so, sbt-one-log comes to free your hands.

## usage 
Add `sbt-one-log` plugin to the sbt configuration:

### add plugin in project/plugins.sbt
```scala
addSbtPlugin("com.zavakid.sbt" % "sbt-one-log" % "0.1")
```
### using build.sbt
```scala
import OneLogKeys._

// oneLogSettings will add libDependencies and resolvers
oneLogSettings
```

Now sbt-one-log will add the log dependency and override other log lib.

### using project/Build.scala
```scala
import sbt._
import sbt.Keys._
import OneLogKeys._

object Build extends sbt.Build {

  // add oneLogSettings to your settings
  lazt val root = Project(
    id = "example",
    base = file(.),
  ).settings(oneLogSettings: _*)

  //... 
  //other settings
  //...
}



```

## for developers

when release a new version, make sure to publish to notes.implicit.ly by [herald][herald] please.

[herald]: https://github.com/n8han/herald

## License

sbt-one-log is under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
