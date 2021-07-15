# `sbt-one-log` plugin [![Build Status](https://travis-ci.org/CSUG/sbt-one-log.svg?branch=master)](https://travis-ci.org/CSUG/sbt-one-log)

`sbt-one-log` is a sbt plugin make logging dependency easy.

`sbt-one-log` plugin provides you an easy way to manage the logging dependency (avoid the logging lib hell):

- Resolve the logging dependencies chaos in your development.
- Just make logging work as you expect and follow the best practice, automatically.
- Keep the dependency available when generate `pom.xml`.

----------------------------------------

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [🔧 Features](#-features)
- [👥 Usage](#-usage)
    - [Add plugin in `project/plugins.sbt`](#add-plugin-in-projectpluginssbt)
    - [Using `build.sbt`](#using-buildsbt)
    - [Using `project/Build.scala`](#using-projectbuildscala)
- [🚚 Release notes](#-release-notes)
- [✨ Why `sbt-one-log`](#-why-sbt-one-log)
- [👩‍🚒 For developers](#%E2%80%8D-for-developers)
    - [Test](#test)
    - [Release](#release)
- [📜 License](#-license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

----------------------------------------

## 🔧 Features

- Automatic uniform your logging dependencies, current support `slf4j` and `logback`, other logging lib will be bridged to `slf4j`.
- `scala-logging` support, if you don't need it, you can turn off the `scala-logging` support.
- Task `generateLogbackXML` to help you generate the `logback.xml` and `logback-test.xml`.

## 👥 Usage

For sbt 0.13.5 or above, if you use sbt under 0.13.5, please use : [0.1.3](https://github.com/CSUG/sbt-one-log/tree/branch-0.1.3)

Add `sbt-one-log` plugin to the sbt configuration:

### Add plugin in `project/plugins.sbt`

```scala
addSbtPlugin("com.zavakid.sbt" % "sbt-one-log" % "1.0.1")
```
### Using `build.sbt`

```scala
// oneLogSettings will add libDependencies and resolvers
lazy val yourProject = (project in file(".")).enablePlugins(SbtOneLog)
```

Now `sbt-one-log` will add the logging dependency and override other logging lib automatically.

### Using `project/Build.scala`

**important: `oneLogSettings` must position after `libraryDependencies`.**

```scala
import sbt._
import sbt.Keys._
import com.zavakid.sbt._

object Build extends sbt.Build {

  // add oneLogSettings to your settings
  lazy val root = Project(
    id = "example",
    base = file(.),
  ).enablePlugins(SbtOneLog)

  //...
  //other settings
  //...
}

```

Now everything is OK.

## 🚚 Release notes

See [sbt-one-log release notes](https://github.com/CSUG/sbt-one-log/tree/master/notes).

## ✨ Why `sbt-one-log`

`Scala` can leverage lots of perfect `Java` lib, but it's chaotic with the logging libs in `Java` world.

Looking at the logging libs below: 😕

- `java.util.logging`
- `commons-logging`
- `commons-logging-api`
- `log4j`
- `slf4j`
- `logback`
- `log4j 2`
- `scala-logging`
- `slf4s` (the latest version only support `Scala` 2.9.1)
- `Grizzled SLF4J`
- `AVSL`
- `loglady`
- `logula` (abandoned)

Of course, you can keep your project dependency cleanly with one or two logging lib (e.g., `slf4j` and `logback`)

But sometimes your other dependencies is out of control.
e.g., if your dependency with `apache httpclient` lib which contains dependency with `commons-logging`, you will log with `commons-logging`

Also, you can add `jcl-over-slf4j` and exclude `commons-logging` explicitly in `libraryDependencies` setting.

A better way is to explicitly declare dependency `commons-logging` with the special version `99-empty`.

So, `sbt-one-log` comes to free your hands.

## 👩‍🚒 For developers

### Test

After (fix bugs)/(add features), please add test case and run test.
to run test, just

```bash
scripts/bump-version.sh 1.x.y

sbt publishLocal
sbt scripted
```

### Release

When release a new version, make sure to publish to `notes.implicit.ly` by [herald][herald] please.

[herald]: https://github.com/n8han/herald

## 📜 License

`sbt-one-log` is under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
