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

  val slf4jApiProcess: ProcessStrategy = { context =>
    val version = context.extracted.get(slf4jVersion in context.p)
    val slf4jApi = "org.slf4j" % "slf4j-api" % version force()
    context.copy(libraryDeps = addOrReplaceModuleId(slf4jApi, context.libraryDeps))
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


  private def isSameArtifact(libA: ModuleID, libB: ModuleID) =
    libA.organization.equals(libB.organization) &&
      libA.name.equals(libB.name)
}
