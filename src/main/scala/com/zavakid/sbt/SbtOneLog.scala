package com.zavakid.sbt

import com.zavakid.sbt.IvyGraphMLDependencies._
import com.zavakid.sbt.LogDepProcess._
import org.apache.ivy.core.resolve.ResolveOptions
import org.fusesource.scalate.TemplateEngine
import sbt.Keys._
import sbt._

/**
 *
 * @author zavakid 2014-11-10
 */
object SbtOneLogKeys {

  val slf4jVersion = settingKey[String]("which slf4j version to use")
  val logbackVersion = settingKey[String]("which logback version to use")
  //val scalaLoggingVersion = settingKey[String]("which scalaLogging version to use")
  val useScalaLogging = settingKey[Boolean]("add the scalaLogging(https://github.com/typesafehub/scala-logging)")
  val logbackXMLTemplate = settingKey[String]("the logback template path")
  val logbackFileName = settingKey[String]("the logback file name")
  //val logbackTestXMLTemplate = settingKey[String]("the logback-test template path")
  val withLogDependencies = settingKey[Seq[sbt.ModuleID]]("with log dependencies")
  val generateLogbackXML = TaskKey[Unit]("generate-logback-xml", "generate logback.xml and logback-test.xml in test if they are not exist")

  // from sbt-dependency-graph
  val computeIvReportFunction = TaskKey[String => File]("compute-ivy-report-function",
    "A function which returns the file containing the ivy report from the ivy cache for a given configuration")
  val computeIvyReport = TaskKey[File]("compute-ivy-report",
    "A task which returns the location of the ivy report file for a given configuration (default `compile`).")
  val computeModuleGraph = TaskKey[ModuleGraph]("compute-module-graph",
    "The dependency graph for a project")
}

object SbtOneLog extends AutoPlugin {

  import com.zavakid.sbt.SbtOneLogKeys._

  val autoImport = SbtOneLogKeys


  override def requires: Plugins = super.requires


  override def globalSettings: Seq[Def.Setting[_]] = {
    onLoad := onLoad.value andThen doTask
    //      onLoad := onLoad.value andThen task

  }

  var appended = false
  val doTask: State => State = { state =>
    if (SbtOneLog.appended)
      state
    else {
      import state.globalLogging.{full => log}
      log.info("sbt-one-log start process...")

      val buildStruct = Project.structure(state)
      val extracted = Project.extract(state)

      def compute(graph: ModuleGraph, libraryDeps: Seq[sbt.ModuleID], p: ProjectRef): IndexedSeq[ModuleID] = {
        val roots = graph.nodes.filter(n => !graph.edges.exists(_._2 == n.id)).sortBy(_.id.idString)
        val directDeps = roots.flatMap(d => graph.dependencyMap(d.id))
          .filter(_.evictedByVersion.isEmpty)
          .filterNot(d => d.id.organisation.equals("org.scala-lang"))
          .flatMap { dep => // filter deps which not contains sub projects
          libraryDeps.find { libDep =>
            libDep.organization.equals(dep.id.organisation) && libDep.name.equals(dep.id.name)
          }.map(dep -> _)
        }

        directDeps.foldLeft(libraryDeps.toIndexedSeq) {
          case (libs, (dep, libDep)) =>
            val context = ProcessContext(dep.id, libDep, graph, libs, p, extracted)
            processStrategies(context).libraryDeps
        }
      }

      val (transformed, newState) = buildStruct.allProjectRefs.filter { p =>
        //FIXME! .task is deprecated
        extracted.getOpt((computeModuleGraph in p).task).isDefined
      }.foldLeft((extracted.session.mergeSettings, state)) { case ((allSettings, foldedState), p) =>
        // need receive new state
        val (newState, depGraph) = extracted.runTask(computeModuleGraph in p, foldedState)
        val newLibs = compute(depGraph, extracted.get(libraryDependencies in p), p)
        (allSettings.map {
          s => s.key.key match {
            //case s if "libraryDependencies".equals(s.key.key.label) =>
            case libraryDependencies.key =>
              // ensure just modify this project's dependencies
              s.key.scope.project.toOption.filter(p.equals(_)).fold(s.asInstanceOf[Setting[Seq[ModuleID]]]) { _ =>
                s.asInstanceOf[Setting[Seq[ModuleID]]].mapInit((_, _) => newLibs)
              }
            case _ => s
          }
        }, newState)
      }

      SbtOneLog.appended = true
      //extracted.append(appendedSettings, state)
      val newStructure = Load.reapply(transformed, extracted.structure)(extracted.showKey)
      log.info("sbt-one-log finished process")
      Project.setProject(extracted.session, newStructure, newState)
    }
  }

//  def task: State => State = { state =>
//    val extracted = Project.extract(state)
//    extracted.structure.allProjectRefs.foldLeft(state) { (state, p) =>
//      val ds: Seq[ModuleID] = extracted.get(libraryDependencies in p)
//      println("=====" + p + " dep : ")
//      ds.foreach(println)
//      println("===========")
//      if (p.project == "module1") {
//        val (newState, _) = extracted.runTask(update.in(p).in(Compile), state)
//        extracted.append(Seq[Setting[_]](
//          libraryDependencies in p := Seq()
//        ), newState)
//      } else state
//
//    }
//  }

  override def projectSettings: Seq[Setting[_]] = Seq[Setting[_]](
    slf4jVersion := "1.7.10"
    , logbackVersion := "1.1.2"
    , useScalaLogging := true
    , resolvers += "99-empty" at "http://version99.qos.ch/"
    //, libraryDependencies ++= logs.value
    , computeIvReportFunction := computeIvReportFunctionImpl.value
    , computeIvyReport <<= computeIvReportFunction map (_(Compile.toString())) dependsOn (update in Compile)
    , computeModuleGraph <<= computeIvyReport map (absoluteReportPath andThen IvyGraphMLDependencies.graph)
  ) ++ inConfig(Compile) {
    Seq(
      logbackXMLTemplate := "/sbtonelog/templates/logback.xml.mustache"
      , logbackFileName := "logback.xml"
      , generateLogbackXML := generateLogbackXMLImpl.value
    )
  } ++ inConfig(Test) {
    Seq(
      logbackXMLTemplate := "/sbtonelog/templates/logback-test.xml.mustache"
      , logbackFileName := "logback-test.xml"
      , generateLogbackXML := generateLogbackXMLImpl.value
    )
  }

  lazy val generateLogbackXMLImpl: Def.Initialize[Task[Unit]] = Def.task {
    val out = streams.value
    def generateContent(engine: TemplateEngine, context: Map[String, Any], templatePath: String, baseDir: File, file: File) {
      val content = engine.layout(templatePath, context)
      if (!baseDir.exists) baseDir.mkdirs()
      file.createNewFile()
      out.log.info(s"generate $file")
      IO.write(file, content)
    }
    //val force = generateLogbackXMLParser.parsed
    val force = false
    val resourceDir = resourceDirectory.value
    val logbackXML = resourceDir / logbackFileName.value
    val context = Map("projectName" -> name.value)
    val engine = new TemplateEngine()
    (force, logbackXML.exists()) match {
      case (false, false) =>
        generateContent(engine, context, logbackXMLTemplate.value, resourceDir, logbackXML)
      case (false, true) =>
        out.log.info(s"${logbackXML.toString} is exist")
      case (true, _) =>
        out.log.warn(s"force generate is not support yes")
    }
  }

  // =============
  // from https://github.com/jrudolph/sbt-dependency-graph/blob/master/src/main/scala/net/virtualvoid/sbt/graph/Plugin.scala
  lazy val computeIvReportFunctionImpl: Def.Initialize[Task[String => File]] = Def.task {
    val (sbtV, target, projectID, ivyModule, config, streams) = (Keys.sbtVersion.value, Keys.target.value, Keys.projectID.value, Keys.ivyModule.value, Keys.appConfiguration.value, Keys.streams.value)
    sbtV match {
      case Version(0, min, fix, _) if min > 12 || (min == 12 && fix >= 3) =>
        (c: String) => file("%s/resolution-cache/reports/%s-%s-%s.xml".format(target, projectID.organization, crossName(ivyModule), c))
      case Version(0, min, fix, _) if min == 12 && fix >= 1 && fix < 3 =>
        ivyModule.withModule(streams.log) { (i, moduleDesc, _) =>
          val id = ResolveOptions.getDefaultResolveId(moduleDesc)
          (c: String) => file("%s/resolution-cache/reports/%s/%s-resolved.xml" format(target, id, c))
        }
      case _ =>
        val home = config.provider.scalaProvider.launcher.ivyHome
        (c: String) => file("%s/cache/%s-%s-%s.xml" format(home, projectID.organization, crossName(ivyModule), c))
    }
  }

  val VersionPattern = """(\d+)\.(\d+)\.(\d+)(?:-(.*))?""".r

  object Version {
    def unapply(str: String): Option[(Int, Int, Int, Option[String])] = str match {
      case VersionPattern(major, minor, fix, appendix) => Some((major.toInt, minor.toInt, fix.toInt, Option(appendix)))
      case _ => None
    }
  }

  // This is to support 0.13.8's InlineConfigurationWithExcludes while not forcing 0.13.8
  // taken from: https://github.com/jrudolph/sbt-dependency-graph/pull/68/files
  type HasModule = {
    val module: ModuleID
  }
  def crossName(ivyModule: IvySbt#Module) = ivyModule.moduleSettings match {
    case ic: InlineConfiguration => ic.module.name
    case hm: HasModule if hm.getClass.getName == "sbt.InlineConfigurationWithExcludes" => hm.module.name
    case _ =>
      throw new IllegalStateException("sbtOneLog plugin currently only supports InlineConfiguration of ivy settings (the default in sbt, from sbt-dependency-graph)")
  }

  def absoluteReportPath = (file: File) => file.getAbsolutePath

}
