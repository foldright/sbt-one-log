addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.3")

libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")

addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")
