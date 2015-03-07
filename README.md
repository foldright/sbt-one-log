# sbt-one-log plugin [![Build Status](https://travis-ci.org/CSUG/sbt-one-log.svg?branch=master)](https://travis-ci.org/CSUG/sbt-one-log)
sbt-one-log is a sbt plugin make log dependency easy.

sbt-one-log plugin provides you a easy way to manage the log dependency ( avoid the log lib hell), and keep the dependency available when generate pom.xml.

## features
* automatic uniform your log dependency, current support slf4j and logback, other log lib will be bridged to slf4j.
* scala-logging support, if you don't need it, you can turn off the scala-logging support
* task `generateLogbackXML` to help you generate the logback.xml and logback-test.xml

## release notes
[sbt-one-log release notes](https://github.com/CSUG/sbt-one-log/tree/master/notes)

## usage 
for sbt 0.13.5 or above, if you use sbt under 0.13.5, please use : [0.1.3](https://github.com/CSUG/sbt-one-log/tree/branch-0.1.3)

Add `sbt-one-log` plugin to the sbt configuration:

### add plugin in project/plugins.sbt
```scala
addSbtPlugin("com.zavakid.sbt" % "sbt-one-log" % "1.0.0")
```
### using build.sbt

```scala
// oneLogSettings will add libDependencies and resolvers
lazy val yourProject = (project in file(".")).enablePlugins(SbtOneLog)
```
Now sbt-one-log will automatic add the log dependency and override other log lib.

### using project/Build.scala
**important: oneLogSettings must position after `libraryDependencies`**

```scala
import sbt._
import sbt.Keys._
import import com.zavakid.sbt._

object Build extends sbt.Build {

  // add oneLogSettings to your settings
  lazt val root = Project(
    id = "example",
    base = file(.),
  ).enablePlugins(SbtOneLog)

  //... 
  //other settings
  //...
}

```

and everything is OK.

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

## for developers

#### test
after (fix bugs)/(add features), please add test case and run test.
to run test, just 
```bash
.bin/bump-version.sh
sbt publishLocal
sbt scripted
```

#### release
when release a new version, make sure to publish to notes.implicit.ly by [herald][herald] please.

[herald]: https://github.com/n8han/herald

## License

sbt-one-log is under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).


