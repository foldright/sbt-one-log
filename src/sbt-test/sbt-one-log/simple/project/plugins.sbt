//{
//  val pluginVersion = System.getProperty("plugin.version")
//  if(pluginVersion == null)
//    throw new RuntimeException(
//      """|The system property 'plugin.version' is not defined.
//         |Specify this property using the scriptedLaunchOpts -D.
//      """.stripMargin)
//  else addSbtPlugin("com.zavakid.sbt" % "sbt-one-log" % pluginVersion)
//}

addSbtPlugin("com.zavakid.sbt" % "sbt-one-log" % "0.1-SNAPSHOT")
