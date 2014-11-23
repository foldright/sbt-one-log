package com.zavakid.sbt

import com.zavakid.sbt.SbtOneLogKeys._
import sbt._
import Keys._

import scala.collection.immutable

/**
 *
 * @author zavakid 2014-11-21
 */
object LogDepProcess {

  case class ProcessContext(directDep: IvyGraphMLDependencies.ModuleId,
                            directLib: ModuleID,
                            graph: IvyGraphMLDependencies.ModuleGraph,
                            libraryDeps: immutable.IndexedSeq[ModuleID],
                            p: ProjectRef,
                            extracted: Extracted
                             )

  type ProcessStrategy = ProcessContext => ProcessContext

  def processStrategies: ProcessStrategy = { context =>
    processStrategySeq.foldLeft(context) { case (c, strategy) =>
      strategy(c)
    }
  }

  def processStrategySeq = Seq(
    scalaLoggingProcess,
    slf4jApiProcess,
    logbackCoreProcess,
    logbackClassicProcess,
    log4jProcess,
    commonLoggingProcess,
    julLogProcess
  )

  val slf4jApiProcess: ProcessStrategy = { context =>
    val version = context.extracted.get(slf4jVersion in context.p)
    val slf4jApi = "org.slf4j" % "slf4j-api" % version force()
    context.copy(libraryDeps = addOrReplaceModuleId(slf4jApi, context.libraryDeps))
  }

  val logbackCoreProcess: ProcessStrategy = { context =>
    val version = context.extracted.get(logbackVersion in context.p)
    val logbackCore = "ch.qos.logback" % "logback-core" % version force()
    context.copy(libraryDeps = addOrReplaceModuleId(logbackCore, context.libraryDeps))
  }

  val logbackClassicProcess: ProcessStrategy = { context =>
    val version = context.extracted.get(logbackVersion in context.p)
    val logbackClassic = "ch.qos.logback" % "logback-classic" % version force()
    context.copy(libraryDeps = addOrReplaceModuleId(logbackClassic, context.libraryDeps))
  }

  val scalaLoggingProcess: ProcessStrategy = { context =>
    if(context.extracted.get(useScalaLogging in context.p))
      context.extracted.get(scalaBinaryVersion in context.p) match {
        case "2.11" =>
          context.copy(libraryDeps = addOrReplaceModuleId("com.typesafe.scala-logging" %% "scala-logging" % "3.1.0", context.libraryDeps))
        case "2.10" =>
          Option(addOrReplaceModuleId("com.typesafe.scala-logging" %% "scala-logging-api" % "2.1.2", context.libraryDeps)).map {
            addOrReplaceModuleId("com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2", _)
          }.map{ libs =>
            context.copy(libraryDeps = libs)
          }.get
      }
    else context
  }

  // find if have log4j:log4j | "org.slf4j" % "slf4j-log4j12" , if have:
  // 1. exclude it( don't exclude twice )
  // 2. exclude "org.slf4j" % "slf4j-log4j12"
  // 3. add log4j:log4j:99-empty
  // 4. add "org.slf4j" % "log4j-over-slf4j"
  // 5. add repo?
  val log4jProcess: ProcessStrategy = { context =>
    val version = context.extracted.get(slf4jVersion in context.p)
    val log4j = "log4j" % "log4j" % "99-empty" force()
    val log4jOverSlf4j = "org.slf4j" % "log4j-over-slf4j" % version force()

    if(haveDependency(context, log4j) || haveDependency(context, "org.slf4j" % "slf4j-log4j12" % "-1")){
      Option(replaceModuleId(excludeForModuleID(context.directLib, "log4j", "log4j"), context.libraryDeps)).map {
        replaceModuleId(excludeForModuleID(context.directLib, "org.slf4j", "slf4j-log4j12"), _)
      }.map{
        addOrReplaceModuleId(log4j, _)
      }.map{
        addOrReplaceModuleId(log4jOverSlf4j, _)
      }.map{
        removeModuleWithDiffVersion("org.slf4j" % "slf4j-log4j12" % "-1" , _)
      }.map{
        removeModuleWithDiffVersion(log4j , _)
      }.map{ libs =>
        context.copy(libraryDeps = libs)
      }.get
    }
    else context
  }

  // find if have "commons-logging" % "commons-logging" |  "commons-logging" % "commons-logging-api" | "org.slf4j" % "slf4j-jcl", if have:
  // 1. exclude them (don't exclude twice)
  // 2. exclude "org.slf4j" % "slf4j-jcl"
  // 2. add "commons-logging" % "commons-logging" % "99-empty" force()
  // 3. add "commons-logging" % "commons-logging-api" % "99-empty" force()
  // 4. add "org.slf4j" % "jcl-over-slf4j" % slf4jVersion.value force()
  // 5.
  val commonLoggingProcess: ProcessStrategy = { context =>
    val version = context.extracted.get(slf4jVersion in context.p)
    val jcl = "commons-logging" % "commons-logging" % "99-empty" force()
    val jclApi = "commons-logging" % "commons-logging-api" % "99-empty" force()
    val jclOverSlf4j = "org.slf4j" % "jcl-over-slf4j" % version force()

    if(haveDependency(context, jcl) || haveDependency(context, jclApi) || haveDependency(context, "org.slf4j" % "slf4j-jcl" % "-1")){
      Option(replaceModuleId(excludeForModuleID(context.directLib, "commons-logging", "commons-logging"), context.libraryDeps)).map{
        replaceModuleId(excludeForModuleID(context.directLib, "commons-logging", "commons-logging-api"), _)
      }.map{
        replaceModuleId(excludeForModuleID(context.directLib, "org.slf4j", "slf4j-jcl"), _)
      }.map{
        addOrReplaceModuleId(jcl, _)
      }.map{
        addOrReplaceModuleId(jclApi, _)
      }.map{
        addOrReplaceModuleId(jclOverSlf4j, _)
      }.map{
        removeModuleWithDiffVersion(jcl , _)
      }.map{
        removeModuleWithDiffVersion(jclApi , _)
      }.map{
        removeModuleWithDiffVersion("org.slf4j" % "slf4j-jcl" % "-1" , _)
      }.map{ libs =>
        context.copy(libraryDeps = libs)
      }.get
    } else context
  }

  // if hava dependency  "org.slf4j" -> "slf4j-jdk14", exclude it
  // add "org.slf4j" % "jul-to-slf4j"
  val julLogProcess: ProcessStrategy = { context =>
    val version = context.extracted.get(slf4jVersion in context.p)
    val julSlf4j = "org.slf4j" % "jul-to-slf4j" % version force()
    val newContext = context.copy(libraryDeps = addOrReplaceModuleId(julSlf4j, context.libraryDeps))
    if(haveDependency(newContext, "org.slf4j" % "slf4j-jdk14" % "-1")){
      Option(replaceModuleId(excludeForModuleID(newContext.directLib, "org.slf4j", "slf4j-jdk14"), newContext.libraryDeps)).map{
        removeModuleWithDiffVersion("org.slf4j" % "slf4j-jdk14" % "-1" , _)
      }.map{ libs =>
        newContext.copy(libraryDeps = libs)
      }.get
    } else newContext
  }


  // ======= helper ======
  def addOrReplaceModuleId(newModule: ModuleID, libraryDeps: immutable.IndexedSeq[ModuleID]): immutable.IndexedSeq[ModuleID] =
    if (libraryDeps.exists(isSameArtifact(newModule, _))) {
      libraryDeps.map { lib =>
        if (isSameArtifact(newModule, lib))
          newModule
        else lib
      }
    } else {
      newModule +: libraryDeps
    }

  def replaceModuleId(newModule: ModuleID, libraryDeps: immutable.IndexedSeq[ModuleID]): immutable.IndexedSeq[ModuleID] =
    if (libraryDeps.exists(isSameArtifact(newModule, _))) {
      libraryDeps.map { lib =>
        if (isSameArtifact(newModule, lib))
          newModule
        else lib
      }
    } else libraryDeps

  //find dependency with not same version
  def haveDependency(context: ProcessContext, find: ModuleID): Boolean = {
    def doFindDep(moduleIds: Seq[IvyGraphMLDependencies.ModuleId], find: ModuleID, graph: IvyGraphMLDependencies.ModuleGraph): Boolean = moduleIds.exists { mid =>
      if (isSameArtifactButDiffVersion(mid, find)) true
      else doFindDep(graph.dependencyMap(mid).map(_.id), find, graph)
    }
    doFindDep(Seq(context.directDep), find, context.graph)
  }

  def removeModuleWithDiffVersion(module: ModuleID, libraryDeps: immutable.IndexedSeq[ModuleID]): immutable.IndexedSeq[ModuleID] =
    libraryDeps.filterNot { m =>
      m.organization.equals(module.organization) &&
        m.name.equals(module.name) &&
        !m.revision.equals(module.revision)
    }

  def excludeForModuleID(module: ModuleID, org: String, name: String): ModuleID =
    if (module.exclusions.exists { e =>
      org.equals(e.organization) && name.equals(e.name)
    }) module
    else
      module.exclude(org, name)


  private def isSameArtifact(libA: ModuleID, libB: ModuleID)
                            (implicit additional: (ModuleID, ModuleID) => Boolean) =
    libA.organization.equals(libB.organization) &&
      libA.name.equals(libB.name)

  private def isSameArtifact(libA: IvyGraphMLDependencies.ModuleId, libB: ModuleID)
                            (implicit additional: (IvyGraphMLDependencies.ModuleId, ModuleID) => Boolean) =
    libA.organisation.equals(libB.organization) &&
      libA.name.equals(libB.name) &&
      additional(libA, libB)

  private def isSameArtifactWithVersion(libA: IvyGraphMLDependencies.ModuleId, libB: ModuleID) =
    isSameArtifact(libA, libB)(isSameVersion)

  private def isSameArtifactButDiffVersion(libA: IvyGraphMLDependencies.ModuleId, libB: ModuleID) =
    isSameArtifact(libA, libB)(isNotSameVersion)

  private def isSameVersion(libA: IvyGraphMLDependencies.ModuleId, libB: ModuleID) =
    libA.version.equals(libB.revision)

  private def isNotSameVersion(libA: IvyGraphMLDependencies.ModuleId, libB: ModuleID) =
    !isSameVersion(libA, libB)

  private implicit def isSameArtifactTrue(a: ModuleID, b: ModuleID): Boolean = true

  private implicit def isSameArtifactTrue(a: IvyGraphMLDependencies.ModuleId, b: ModuleID): Boolean = true
}

