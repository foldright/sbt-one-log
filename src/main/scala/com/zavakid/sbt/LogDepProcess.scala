package com.zavakid.sbt

import com.zavakid.sbt.SbtOneLogKeys._
import sbt._

import scala.collection.immutable

/**
 *
 * @author zebin.xuzb 2014-11-21
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
    slf4jApiProcess,
    logbackCoreProcess,
    logbackClassicProcess
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

  // find if have log4j:log4j:_ dependency, have have:
  // 1. exclude it( don't exclude twice )
  // 2. add log4j:log4j:99-empty
  // 3. add "org.slf4j" % "log4j-over-slf4j"
  // 4. add repo?
  val log4jProcess: ProcessStrategy = { context =>
    val slf4j = "log4j" % "log4j" % "99-empty" force()

    ???
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

  private implicit def isSameArtifactTrue(a: ModuleID, b: ModuleID): Boolean = true
  private implicit def isSameArtifactTrue(a: IvyGraphMLDependencies.ModuleId, b: ModuleID): Boolean = true
}

