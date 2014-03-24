addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.3")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)
